package com.scudata.dm.op;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.Node;
import com.scudata.util.Variant;

class PrimaryJoinItem {
	private Context ctx; // ��ǰ������õ�������
	private ICursor cursor;
	private Expression []keyExps;
	private Expression []newExps;
	private Node []gathers = null; // ͳ�Ʊ��ʽ�е�ͳ�ƺ���
	private int joinType; // �������ͣ�0:������, 1:������, 2�������㣬����ƥ�䲻�ϵ�
	
	private Current current; // ��ǰ�����������ѹջ
	private Sequence data; // �α�ȡ��������
	private Object []keyValues;
	private Object []cacheKeyValues; // ��һ����¼�ļ�ֵ�����ڴ�ʱ�������ܵĹ���
	
	private int keyCount = 0;
	private int newCount = 0; // new�ֶ���
	private int seq;	// ��ǰ��¼��data�е����

	private boolean isGather = false; // �����ı�ļ�����ʽ�Ƿ��ǻ��ܱ��ʽ
	private boolean isPrevMatch = false; // ��ǰ��¼�Ǽ�ֵ�Ƿ����һ������¼�ļ�ֵƥ��
	
	public PrimaryJoinItem(ICursor cursor, Expression []keyExps, Expression []newExps, int joinType, Context ctx) {
		this.ctx = new Context(ctx);
		this.cursor = cursor;
		this.keyExps = keyExps;
		this.newExps = newExps;
		this.joinType = joinType;
		
		if (newExps != null) {
			newCount = newExps.length;
			for (Expression exp : newExps) {
				if (exp.getHome() instanceof Gather) {
					gathers = Sequence.prepareGatherMethods(newExps, ctx);
					isGather = true;
					break;
				}
			}
		}
		
		keyCount = keyExps.length;
		keyValues = new Object[keyCount];
		cacheKeyValues = new Object[keyCount];
		cacheData();
	}
	
	private void cacheData() {
		data = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
		if (data != null && data.length() > 0) {
			ComputeStack stack = ctx.getComputeStack();
			if (current != null) {
				stack.pop();
			}
			
			current = new Current(data, 1);
			stack.push(current);
			seq = 1;
			
			for (int i = 0; i < keyCount; ++i) {
				keyValues[i] = keyExps[i].calculate(ctx);
			}
		} else {
			seq = -1;
		}
	}
	
	public Object[] getCurrentKeyValues() {
		if (seq > 0) {
			return keyValues;
		} else {
			return null;
		}
	}
	
	private void calcNewValues(Object []srcKeyValues, Object []resultValues, int fieldIndex) {
		int newCount = this.newCount;
		if (newCount == 0) {
			return;
		}
		
		Context ctx = this.ctx;
		Node []gathers = this.gathers;
		
		if (isGather) {
			isPrevMatch = false;
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = gathers[i].gather(ctx);
			}
			
			Expression []keyExps = this.keyExps;
			Object []keyValues = this.keyValues;
			int keyCount = this.keyCount;
			
			while (true) {
				seq++;
				if (seq > data.length()) {
					cacheData();
					if (seq == -1) {
						break;
					}
				} else {
					current.setCurrent(seq);
					for (int i = 0; i < keyCount; ++i) {
						keyValues[i] = keyExps[i].calculate(ctx);
					}
				}
				
				if (Variant.compareArrays(srcKeyValues, keyValues, keyCount) == 0) {
					for (int i = 0; i < newCount; ++i) {
						resultValues[fieldIndex + i] = gathers[i].gather(resultValues[fieldIndex + i], ctx);
					}
				} else {
					break;
				}
			}
			
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = gathers[i].finish(resultValues[fieldIndex + i]);
			}
		} else {
			Expression []newExps = this.newExps;
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = newExps[i].calculate(ctx);
			}
		}
	}
	
	public void resetNewValues(Object []resultValues, int fieldIndex) {
		for (int i = 0, count = newCount; i < count; ++i) {
			resultValues[fieldIndex++] = null;
		}
	}
	
	public void popTop(Object []resultValues, int fieldIndex) {
		int newCount = this.newCount;
		Context ctx = this.ctx;
		Node []gathers = this.gathers;
		
		if (isGather) {
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = gathers[i].gather(ctx);
			}
			
			Expression []keyExps = this.keyExps;
			Object []keyValues = this.keyValues;
			Object []prevKeyValues = this.cacheKeyValues;
			int keyCount = this.keyCount;
			System.arraycopy(keyValues, 0, prevKeyValues, 0, keyCount);
			
			while (true) {
				seq++;
				if (seq > data.length()) {
					cacheData();
					if (seq == -1) {
						break;
					}
				} else {
					current.setCurrent(seq);
					for (int i = 0; i < keyCount; ++i) {
						keyValues[i] = keyExps[i].calculate(ctx);
					}
				}
				
				if (Variant.compareArrays(prevKeyValues, keyValues, keyCount) == 0) {
					for (int i = 0; i < newCount; ++i) {
						resultValues[fieldIndex + i] = gathers[i].gather(resultValues[fieldIndex + i], ctx);
					}
				} else {
					break;
				}
			}
			
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = gathers[i].finish(resultValues[fieldIndex + i]);
			}
		} else {
			Expression []newExps = this.newExps;
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = newExps[i].calculate(ctx);
			}
			
			seq++;
			if (seq > data.length()) {
				cacheData();
			} else {
				current.setCurrent(seq);
				for (int i = 0; i < keyCount; ++i) {
					keyValues[i] = keyExps[i].calculate(ctx);
				}
			}
		}
	}
	
	public boolean join(Object []srcKeyValues, Object []resultValues, int fieldIndex) {
		if (seq == -1) {
			return joinType != 0;
		}

		Expression []keyExps = this.keyExps;
		Object []keyValues = this.keyValues;
		int keyCount = this.keyCount;
		
		if (isPrevMatch) {
			isPrevMatch = false;
			seq++;
			
			if (seq > data.length()) {
				cacheData();
				if (seq == -1) {
					resetNewValues(resultValues, fieldIndex);
					return joinType != 0;
				}
			} else {
				current.setCurrent(seq);
				for (int i = 0; i < keyCount; ++i) {
					keyValues[i] = keyExps[i].calculate(ctx);
				}
			}
		}
		
		while (true) {
			int cmp = Variant.compareArrays(srcKeyValues, keyValues, keyCount);
			if (cmp == 0) {
				isPrevMatch = true;
				if (joinType == 2) {
					resetNewValues(resultValues, fieldIndex);
					return false;
				} else {
					calcNewValues(srcKeyValues, resultValues, fieldIndex);
					return true;
				}
			} else if (cmp > 0) {
				seq++;
				if (seq > data.length()) {
					cacheData();
					if (seq == -1) {
						resetNewValues(resultValues, fieldIndex);
						return joinType != 0;
					}
				} else {
					current.setCurrent(seq);
					for (int i = 0; i < keyCount; ++i) {
						keyValues[i] = keyExps[i].calculate(ctx);
					}
				}
			} else {
				resetNewValues(resultValues, fieldIndex);
				return joinType != 0;
			}
		}
	}
	
	public boolean timeKeyJoin(Object []srcKeyValues, Object []resultValues, int fieldIndex) {
		if (seq == -1) {
			resetNewValues(resultValues, fieldIndex);
			return joinType != 0;
		}
		
		int keyCount = this.keyCount;
		int timeIndex = keyCount - 1;
		Expression []keyExps = this.keyExps;
		Object []keyValues = this.keyValues;
		
		while (true) {
			// �ȱȽϳ�ȥʱ���֮�������ֶεĴ�С
			int cmp = Variant.compareArrays(srcKeyValues, keyValues, timeIndex);
			if (cmp < 0) {
				// û���ܹ����ϵ�
				resetNewValues(resultValues, fieldIndex);
				return joinType != 0;
			} else if (cmp > 0) {
				// ά��ֵС��������һ����¼
				seq++;
				if (seq > data.length()) {
					cacheData();
					if (seq == -1) {
						resetNewValues(resultValues, fieldIndex);
						return joinType != 0;
					}
				} else {
					current.setCurrent(seq);
					for (int i = 0; i < keyCount; ++i) {
						keyValues[i] = keyExps[i].calculate(ctx);
					}
				}
				
				continue;
			}
			
			// ��ƥ���ϣ��Ƚ�ʱ����Ĵ�С
			cmp = Variant.compare(srcKeyValues[timeIndex], keyValues[timeIndex], true);
			if (cmp < 0) {
				// ʱ���û����ƥ���
				resetNewValues(resultValues, fieldIndex);
				return joinType != 0;
			} else if (cmp == 0) {
				if (joinType == 2) {
					resetNewValues(resultValues, fieldIndex);
					return false;
				} else {
					calcNewValues(srcKeyValues, resultValues, fieldIndex);
					return true;
				}
			}
			
			// �ҵ�ʱ�������
			Next:
			while (true) {
				int len = data.length();
				for (int q = seq + 1; q <= len; ++q) {
					current.setCurrent(q);
					for (int i = 0; i < timeIndex; ++i) {
						if (!Variant.isEquals(keyValues[i], keyExps[i].calculate(ctx))) {
							seq = q - 1;
							current.setCurrent(seq);
							break Next;
						}
					}
					
					Object time = keyExps[timeIndex].calculate(ctx);
					cmp = Variant.compare(srcKeyValues[timeIndex], time, true);
					if (cmp < 0) {
						seq = q - 1;
						current.setCurrent(seq);
						break Next;
					} else if (cmp == 0) {
						seq = q;
						keyValues[timeIndex] = time;
						break Next;
					} else {
						keyValues[timeIndex] = time;
					}
				}
				
				Sequence prevData = data;
				int prevSeq = len;
				System.arraycopy(keyValues, 0, cacheKeyValues, 0, keyCount);
				cacheData();
				
				if (seq == -1) {
					data = prevData;
					seq = prevSeq;
					System.arraycopy(cacheKeyValues, 0, keyValues, 0, keyCount);
					break;
				} else {
					cmp = Variant.compareArrays(srcKeyValues, keyValues, keyCount);
					if (cmp < 0) {
						data.insert(1, prevData.getMem(prevSeq));
						System.arraycopy(cacheKeyValues, 0, keyValues, 0, keyCount);
						break;
					} else if (cmp == 0) {
						if (joinType == 2) {
							resetNewValues(resultValues, fieldIndex);
							return false;
						} else {
							calcNewValues(srcKeyValues, resultValues, fieldIndex);
							return true;
						}
					}
				}
			}

			if (joinType == 2) {
				resetNewValues(resultValues, fieldIndex);
				return false;
			} else {
				calcNewValues(srcKeyValues, resultValues, fieldIndex);
				return true;
			}
		}
	}
}
