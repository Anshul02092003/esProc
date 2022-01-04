package com.scudata.dm.cursor;

import com.scudata.dm.Context;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.util.Variant;

/**
 * ���ṹ�������α�������鲢�����γɵ��α�
 * �����Ƕ�MergeCursor���Ż������鲢���α�Ϊ����ʱ�ô��ദ��
 * [cs1,cs2].mergex(xi,��)
 * @author RunQian
 *
 */
public class MergeCursor2 extends ICursor {
	private ICursor cs1; // �α��������Ѿ����鲢�ֶ���������
	private ICursor cs2; // �α��������Ѿ����鲢�ֶ���������
	private int []fields; // ���ֶι鲢�ֶ�
	private int field = -1; // ���ֶι鲢ʱʹ��
	
	private Sequence data1; // ��һ���α�Ļ�������
	private Sequence data2; // �ڶ����α�Ļ�������
	private int cur1 = -1; // ��һ���α굱ǰ�����������ţ�-1��ʾ��û��ȡ����0��ʾȡ�����
	private int cur2 = -1; // �ڶ����α굱ǰ�����������ţ�-1��ʾ��û��ȡ����0��ʾȡ�����
	
	private boolean isNullMin = true; // null�Ƿ���Сֵ
	
	/**
	 * ���������α�Ĺ鲢�α�
	 * @param cs1 ��һ���α�
	 * @param cs2 �ڶ����α�
	 * @param fields �鲢�ֶ��������
	 * @param opt ѡ��
	 * @param ctx ����������
	 */
	public MergeCursor2(ICursor cs1, ICursor cs2, int []fields, String opt, Context ctx) {
		this.cs1 = cs1;
		this.cs2 = cs2;
		this.fields = fields;
		this.ctx = ctx;
		if (fields.length == 1) {
			field = fields[0];
		}
		
		setDataStruct(cs1.getDataStruct());
		
		if (opt != null && opt.indexOf('0') !=-1) {
			isNullMin = false;
		}
	}
	
	public ICursor getCursor1() {
		return cs1;
	}
	
	public ICursor getCursor2() {
		return cs2;
	}
	
	// ���м���ʱ��Ҫ�ı�������
	// �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	protected void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			cs1.resetContext(ctx);
			cs2.resetContext(ctx);
			super.resetContext(ctx);
		}
	}

	private void getData() {
		if (cur1 == -1) {
			data1 = cs1.fetch(FETCHCOUNT_M);
			data2 = cs2.fetch(FETCHCOUNT_M);
			if (data1 != null && data1.length() > 0) {
				cur1 = 1;
			} else {
				cur1 = 0;
			}
			
			if (data2 != null && data2.length() > 0) {
				cur2 = 1;
			} else {
				cur2 = 0;
			}
		}
	}
	
	private Sequence get1(int n, int field) {
		Sequence table;
		if (n > INITSIZE) {
			table = new Sequence(INITSIZE);
		} else {
			table = new Sequence(n);
		}
		
		Sequence data1 = this.data1;
		Sequence data2 = this.data2;
		int cur1 = this.cur1;
		int cur2 = this.cur2;
		
		if (cur1 != 0 && cur2 != 0) {
			int len1 = data1.length();
			int len2 = data2.length();
			Record r1 = (Record)data1.getMem(cur1);
			Record r2 = (Record)data2.getMem(cur2);
			Object v1 = r1.getNormalFieldValue(field);
			Object v2 = r2.getNormalFieldValue(field);
			
			if (isNullMin) {
				for (int i = 0; i < n; ++i) {
					if (Variant.compare(v1, v2, true) > 0) {
						table.add(r2);
						if (cur2 == len2) {
							data2 = cs2.fetch(FETCHCOUNT_M);
							if (data2 != null && data2.length() > 0) {
								cur2 = 1;
								len2 = data2.length();
								r2 = (Record)data2.getMem(1);
								v2 = r2.getNormalFieldValue(field);
							} else {
								cur2 = 0;
								break;
							}
						} else {
							r2 = (Record)data2.getMem(++cur2);
							v2 = r2.getNormalFieldValue(field);
						}
					} else {
						table.add(r1);
						if (cur1 == len1) {
							data1 = cs1.fetch(FETCHCOUNT_M);
							if (data1 != null && data1.length() > 0) {
								cur1 = 1;
								len1 = data1.length();
								r1 = (Record)data1.getMem(1);
								v1 = r1.getNormalFieldValue(field);
							} else {
								cur1 = 0;
								break;
							}
						} else {
							r1 = (Record)data1.getMem(++cur1);
							v1 = r1.getNormalFieldValue(field);
						}
					}
				}
			} else {
				for (int i = 0; i < n; ++i) {
					if (Variant.compare_0(v1, v2) > 0) {
						table.add(r2);
						if (cur2 == len2) {
							data2 = cs2.fetch(FETCHCOUNT_M);
							if (data2 != null && data2.length() > 0) {
								cur2 = 1;
								len2 = data2.length();
								r2 = (Record)data2.getMem(1);
								v2 = r2.getNormalFieldValue(field);
							} else {
								cur2 = 0;
								break;
							}
						} else {
							r2 = (Record)data2.getMem(++cur2);
							v2 = r2.getNormalFieldValue(field);
						}
					} else {
						table.add(r1);
						if (cur1 == len1) {
							data1 = cs1.fetch(FETCHCOUNT_M);
							if (data1 != null && data1.length() > 0) {
								cur1 = 1;
								len1 = data1.length();
								r1 = (Record)data1.getMem(1);
								v1 = r1.getNormalFieldValue(field);
							} else {
								cur1 = 0;
								break;
							}
						} else {
							r1 = (Record)data1.getMem(++cur1);
							v1 = r1.getNormalFieldValue(field);
						}
					}
				}
			}
		}
		
		if (cur1 != 0) {
			int len1 = data1.length();
			for (int i = table.length(); i < n; ++i) {
				table.add(data1.getMem(cur1));
				if (cur1 < len1) {
					cur1++;
				} else {
					data1 = cs1.fetch(FETCHCOUNT_M);
					if (data1 != null && data1.length() > 0) {
						cur1 = 1;
						len1 = data1.length();
					} else {
						cur1 = 0;
						break;
					}
				}
			}
		} else if (cur2 != 0) {
			int len2 = data2.length();
			for (int i = table.length(); i < n; ++i) {
				table.add(data2.getMem(cur2));
				if (cur2 < len2) {
					cur2++;
				} else {
					data2 = cs2.fetch(FETCHCOUNT_M);
					if (data2 != null && data2.length() > 0) {
						cur2 = 1;
						len2 = data2.length();
					} else {
						cur2 = 0;
						break;
					}
				}
			}
		}
		
		this.data1 = data1;
		this.data2 = data2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		
		if (table.length() > 0) {
			return table;
		} else {
			return null;
		}
	}
	
	private Sequence get2(int n, int []fields) {
		Sequence table;
		if (n > INITSIZE) {
			table = new Sequence(INITSIZE);
		} else {
			table = new Sequence(n);
		}
		
		int fcount = fields.length;
		Sequence data1 = this.data1;
		Sequence data2 = this.data2;
		int cur1 = this.cur1;
		int cur2 = this.cur2;
		Object []v1 = new Object[fcount];
		Object []v2 = new Object[fcount];
		
		if (cur1 != 0 && cur2 != 0) {
			int len1 = data1.length();
			int len2 = data2.length();
			Record r1 = (Record)data1.getMem(cur1);
			Record r2 = (Record)data2.getMem(cur2);
			
			for (int f = 0; f < fcount; ++f) {
				v1[f] = r1.getNormalFieldValue(fields[f]);
			}

			for (int f = 0; f < fcount; ++f) {
				v2[f] = r2.getNormalFieldValue(fields[f]);
			}
			
			if (isNullMin) {
				for (int i = 0; i < n; ++i) {
					if (Variant.compareArrays(v1, v2) > 0) {
						table.add(r2);
						if (cur2 == len2) {
							data2 = cs2.fetch(FETCHCOUNT_M);
							if (data2 != null && data2.length() > 0) {
								cur2 = 1;
								len2 = data2.length();
								r2 = (Record)data2.getMem(1);

								for (int f = 0; f < fcount; ++f) {
									v2[f] = r2.getNormalFieldValue(fields[f]);
								}
							} else {
								cur2 = 0;
								break;
							}
						} else {
							r2 = (Record)data2.getMem(++cur2);
							for (int f = 0; f < fcount; ++f) {
								v2[f] = r2.getNormalFieldValue(fields[f]);
							}
						}
					} else {
						table.add(r1);
						if (cur1 == len1) {
							data1 = cs1.fetch(FETCHCOUNT_M);
							if (data1 != null && data1.length() > 0) {
								cur1 = 1;
								len1 = data1.length();
								r1 = (Record)data1.getMem(1);

								for (int f = 0; f < fcount; ++f) {
									v1[f] = r1.getNormalFieldValue(fields[f]);
								}
							} else {
								cur1 = 0;
								break;
							}
						} else {
							r1 = (Record)data1.getMem(++cur1);
							for (int f = 0; f < fcount; ++f) {
								v1[f] = r1.getNormalFieldValue(fields[f]);
							}
						}
					}
				}
			} else {
				for (int i = 0; i < n; ++i) {
					if (Variant.compareArrays_0(v1, v2) > 0) {
						table.add(r2);
						if (cur2 == len2) {
							data2 = cs2.fetch(FETCHCOUNT_M);
							if (data2 != null && data2.length() > 0) {
								cur2 = 1;
								len2 = data2.length();
								r2 = (Record)data2.getMem(1);

								for (int f = 0; f < fcount; ++f) {
									v2[f] = r2.getNormalFieldValue(fields[f]);
								}
							} else {
								cur2 = 0;
								break;
							}
						} else {
							r2 = (Record)data2.getMem(++cur2);
							for (int f = 0; f < fcount; ++f) {
								v2[f] = r2.getNormalFieldValue(fields[f]);
							}
						}
					} else {
						table.add(r1);
						if (cur1 == len1) {
							data1 = cs1.fetch(FETCHCOUNT_M);
							if (data1 != null && data1.length() > 0) {
								cur1 = 1;
								len1 = data1.length();
								r1 = (Record)data1.getMem(1);

								for (int f = 0; f < fcount; ++f) {
									v1[f] = r1.getNormalFieldValue(fields[f]);
								}
							} else {
								cur1 = 0;
								break;
							}
						} else {
							r1 = (Record)data1.getMem(++cur1);

							for (int f = 0; f < fcount; ++f) {
								v1[f] = r1.getNormalFieldValue(fields[f]);
							}
						}
					}
				}
			}
		}
		
		if (cur1 != 0) {
			int len1 = data1.length();
			for (int i = table.length(); i < n; ++i) {
				table.add(data1.getMem(cur1));
				if (cur1 < len1) {
					cur1++;
				} else {
					data1 = cs1.fetch(FETCHCOUNT_M);
					if (data1 != null && data1.length() > 0) {
						cur1 = 1;
						len1 = data1.length();
					} else {
						cur1 = 0;
						break;
					}
				}
			}
		} else if (cur2 != 0) {
			int len2 = data2.length();
			for (int i = table.length(); i < n; ++i) {
				table.add(data2.getMem(cur2));
				if (cur2 < len2) {
					cur2++;
				} else {
					data2 = cs2.fetch(FETCHCOUNT_M);
					if (data2 != null && data2.length() > 0) {
						cur2 = 1;
						len2 = data2.length();
					} else {
						cur2 = 0;
						break;
					}
				}
			}
		}
		
		this.data1 = data1;
		this.data2 = data2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		
		if (table.length() > 0) {
			return table;
		} else {
			return null;
		}
	}
	
	protected Sequence get(int n) {
		if (n < 1) return null;
		getData();
		
		if (field != -1) {
			return get1(n, field);
		} else {
			return get2(n, fields);
		}
	}

	private long skip1(long n, int field) {
		long count = 0;
		Sequence data1 = this.data1;
		Sequence data2 = this.data2;
		int cur1 = this.cur1;
		int cur2 = this.cur2;
		
		if (cur1 != 0 && cur2 != 0) {
			int len1 = data1.length();
			int len2 = data2.length();
			Record r1 = (Record)data1.getMem(cur1);
			Record r2 = (Record)data2.getMem(cur2);
			Object v1 = r1.getNormalFieldValue(field);
			Object v2 = r2.getNormalFieldValue(field);
			
			if (isNullMin) {
				for (; count < n; ++count) {
					if (Variant.compare(v1, v2, true) > 0) {
						if (cur2 == len2) {
							data2 = cs2.fetch(FETCHCOUNT_M);
							if (data2 != null && data2.length() > 0) {
								cur2 = 1;
								len2 = data2.length();
								r2 = (Record)data2.getMem(1);
								v2 = r2.getNormalFieldValue(field);
							} else {
								cur2 = 0;
								break;
							}
						} else {
							r2 = (Record)data2.getMem(++cur2);
							v2 = r2.getNormalFieldValue(field);
						}
					} else {
						if (cur1 == len1) {
							data1 = cs1.fetch(FETCHCOUNT_M);
							if (data1 != null && data1.length() > 0) {
								cur1 = 1;
								len1 = data1.length();
								r1 = (Record)data1.getMem(1);
								v1 = r1.getNormalFieldValue(field);
							} else {
								cur1 = 0;
								break;
							}
						} else {
							r1 = (Record)data1.getMem(++cur1);
							v1 = r1.getNormalFieldValue(field);
						}
					}
				}
			} else {
				for (; count < n; ++count) {
					if (Variant.compare_0(v1, v2) > 0) {
						if (cur2 == len2) {
							data2 = cs2.fetch(FETCHCOUNT_M);
							if (data2 != null && data2.length() > 0) {
								cur2 = 1;
								len2 = data2.length();
								r2 = (Record)data2.getMem(1);
								v2 = r2.getNormalFieldValue(field);
							} else {
								cur2 = 0;
								break;
							}
						} else {
							r2 = (Record)data2.getMem(++cur2);
							v2 = r2.getNormalFieldValue(field);
						}
					} else {
						if (cur1 == len1) {
							data1 = cs1.fetch(FETCHCOUNT_M);
							if (data1 != null && data1.length() > 0) {
								cur1 = 1;
								len1 = data1.length();
								r1 = (Record)data1.getMem(1);
								v1 = r1.getNormalFieldValue(field);
							} else {
								cur1 = 0;
								break;
							}
						} else {
							r1 = (Record)data1.getMem(++cur1);
							v1 = r1.getNormalFieldValue(field);
						}
					}
				}
			}
		}
		
		if (cur1 != 0) {
			int len1 = data1.length();
			for (; count < n; ++count) {
				if (cur1 < len1) {
					cur1++;
				} else {
					data1 = cs1.fetch(FETCHCOUNT_M);
					if (data1 != null && data1.length() > 0) {
						cur1 = 1;
						len1 = data1.length();
					} else {
						cur1 = 0;
						break;
					}
				}
			}
		} else if (cur2 != 0) {
			int len2 = data2.length();
			for (; count < n; ++count) {
				if (cur2 < len2) {
					cur2++;
				} else {
					data2 = cs2.fetch(FETCHCOUNT_M);
					if (data2 != null && data2.length() > 0) {
						cur2 = 1;
						len2 = data1.length();
					} else {
						cur2 = 0;
						break;
					}
				}
			}
		}
		
		this.data1 = data1;
		this.data2 = data2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		return count;
	}
	
	private long skip2(long n, int []fields) {
		long count = 0;
		int fcount = fields.length;
		Sequence data1 = this.data1;
		Sequence data2 = this.data2;
		int cur1 = this.cur1;
		int cur2 = this.cur2;
		Object []v1 = new Object[fcount];
		Object []v2 = new Object[fcount];
		
		if (cur1 != 0 && cur2 != 0) {
			int len1 = data1.length();
			int len2 = data2.length();
			Record r1 = (Record)data1.getMem(cur1);
			Record r2 = (Record)data2.getMem(cur2);
			
			for (int f = 0; f < fcount; ++f) {
				v1[f] = r1.getNormalFieldValue(fields[f]);
			}

			for (int f = 0; f < fcount; ++f) {
				v2[f] = r2.getNormalFieldValue(fields[f]);
			}
			
			if (isNullMin) {
				for (; count < n; ++count) {
					if (Variant.compareArrays(v1, v2) > 0) {
						if (cur2 == len2) {
							data2 = cs2.fetch(FETCHCOUNT_M);
							if (data2 != null && data2.length() > 0) {
								cur2 = 1;
								len2 = data2.length();
								r2 = (Record)data2.getMem(1);

								for (int f = 0; f < fcount; ++f) {
									v2[f] = r2.getNormalFieldValue(fields[f]);
								}
							} else {
								cur2 = 0;
								break;
							}
						} else {
							r2 = (Record)data2.getMem(++cur2);
							for (int f = 0; f < fcount; ++f) {
								v2[f] = r2.getNormalFieldValue(fields[f]);
							}
						}
					} else {
						if (cur1 == len1) {
							data1 = cs1.fetch(FETCHCOUNT_M);
							if (data1 != null && data1.length() > 0) {
								cur1 = 1;
								len1 = data1.length();
								r1 = (Record)data1.getMem(1);

								for (int f = 0; f < fcount; ++f) {
									v1[f] = r1.getNormalFieldValue(fields[f]);
								}
							} else {
								cur1 = 0;
								break;
							}
						} else {
							r1 = (Record)data1.getMem(++cur1);
							for (int f = 0; f < fcount; ++f) {
								v1[f] = r1.getNormalFieldValue(fields[f]);
							}
						}
					}
				}
			} else {
				for (; count < n; ++count) {
					if (Variant.compareArrays_0(v1, v2) > 0) {
						if (cur2 == len2) {
							data2 = cs2.fetch(FETCHCOUNT_M);
							if (data2 != null && data2.length() > 0) {
								cur2 = 1;
								len2 = data2.length();
								r2 = (Record)data2.getMem(1);

								for (int f = 0; f < fcount; ++f) {
									v2[f] = r2.getNormalFieldValue(fields[f]);
								}
							} else {
								cur2 = 0;
								break;
							}
						} else {
							r2 = (Record)data2.getMem(++cur2);
							for (int f = 0; f < fcount; ++f) {
								v2[f] = r2.getNormalFieldValue(fields[f]);
							}
						}
					} else {
						if (cur1 == len1) {
							data1 = cs1.fetch(FETCHCOUNT_M);
							if (data1 != null && data1.length() > 0) {
								cur1 = 1;
								len1 = data1.length();
								r1 = (Record)data1.getMem(1);

								for (int f = 0; f < fcount; ++f) {
									v1[f] = r1.getNormalFieldValue(fields[f]);
								}
							} else {
								cur1 = 0;
								break;
							}
						} else {
							r1 = (Record)data1.getMem(++cur1);

							for (int f = 0; f < fcount; ++f) {
								v1[f] = r1.getNormalFieldValue(fields[f]);
							}
						}
					}
				}
			}
		}
		
		if (cur1 != 0) {
			int len1 = data1.length();
			for (; count < n; ++count) {
				if (cur1 < len1) {
					cur1++;
				} else {
					data1 = cs1.fetch(FETCHCOUNT_M);
					if (data1 != null && data1.length() > 0) {
						cur1 = 1;
						len1 = data1.length();
					} else {
						cur1 = 0;
						break;
					}
				}
			}
		} else if (cur2 != 0) {
			int len2 = data2.length();
			for (; count < n; ++count) {
				if (cur2 < len2) {
					cur2++;
				} else {
					data2 = cs2.fetch(FETCHCOUNT_M);
					if (data2 != null && data2.length() > 0) {
						cur2 = 1;
						len2 = data1.length();
					} else {
						cur2 = 0;
						break;
					}
				}
			}
		}
		
		this.data1 = data1;
		this.data2 = data2;
		this.cur1 = cur1;
		this.cur2 = cur2;
		return count;
	}
	
	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		if (n < 1) return 0;
		getData();
		
		if (field != -1) {
			return skip1(n, field);
		} else {
			return skip2(n, fields);
		}
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		cs1.close();
		cs2.close();
		cur1 = 0;
		cur2 = 0;
		data1 = null;
		data2 = null;
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		close();
		
		if (!cs1.reset() || !cs2.reset()) {
			return false;
		}
		
		cur1 = -1;
		cur2 = -1;
		return true;
	}

	/**
	 * ȡ�����ֶ����
	 * @return �ֶ��������
	 */
	public int[] getFields() {
		return fields;
	}

	/**
	 * ȡ�����ֶ���
	 * @return �ֶ�������
	 */
	public String[] getSortFields() {
		return cs1.getSortFields();
	}
}
