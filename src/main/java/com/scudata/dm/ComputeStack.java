package com.scudata.dm;

import java.util.NoSuchElementException;

/**
 * �����ջ
 * @author WangXiaoJun
 *
 */
public class ComputeStack {
	private LinkEntry<IComputeItem> stackHead; // ���ڼ����е����С���¼����ɶ�ջ
	private LinkEntry<Sequence.Current> argHead; // eval�����õ��Ĳ�����ջ
	
	/**
	 * �Ѳ���ѹջ
	 * @param value Sequence ?������ֵ
	 */
	public void pushArg(Sequence value) {
		if (value != null) {
			argHead = new LinkEntry<Sequence.Current>(value.new Current(), argHead);
		} else {
			argHead = new LinkEntry<Sequence.Current>(null, argHead);
		}
	}

	/**
	 * �Ѳ�����ջ
	 */
	public void popArg() {
		if (argHead != null) {
			argHead = argHead.getNext();
		} else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * ȡ����"arg"��ֵ
	 * @return Sequence.Current
	 */
	public Sequence.Current getArg() {
		if (argHead != null) {
			return argHead.getElement();
		} else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * ������ѹջ��A.(...), r.(...)
	 * @param obj IComputeItem
	 */
	public void push(IComputeItem obj) {
		stackHead = new LinkEntry<IComputeItem>(obj, stackHead);
	}

	// ȡ���еĵ�ǰԪ��ֵ
	public Object getCurrentValue(Sequence seq) {
		for (LinkEntry<IComputeItem> entry = stackHead; entry != null; entry = entry.getNext()) {
			IComputeItem item = entry.getElement();
			if (item.getCurrentSequence() == seq) {
				return item.getCurrent();
			}
		}

		if (seq.length() > 0) {
			return seq.get(1);
		} else {
			return null;
		}
	}

	public Sequence.Current getSequenceCurrent(Sequence seq) {
		for (LinkEntry<IComputeItem> entry = stackHead; entry != null; entry = entry.getNext()) {
			IComputeItem item = entry.getElement();
			if (item.getCurrentSequence() == seq) {
				return (Sequence.Current)item;
			}
		}

		return null;
	}

	// �������еĵ�ǰ����
	public int getCurrentIndex(Sequence seq) {
		for (LinkEntry<IComputeItem> entry = stackHead; entry != null; entry = entry.getNext()) {
			IComputeItem item = entry.getElement();
			if (item.getCurrentSequence() == seq) {
				return item.getCurrentIndex();
			}
		}

		return 0;
	}

	/**
	 * ��ջ���Ķ����ջ
	 */
	public void pop() {
		if (stackHead != null) {
			stackHead.getElement().popStack();
			stackHead = stackHead.getNext();
		} else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * ȡջ���Ķ���
	 * @return IComputeItem
	 */
	public IComputeItem getTopObject() {
		if (stackHead != null) {
			return stackHead.getElement();
		} else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * �ж϶����Ƿ���ջ��
	 * @param obj IComputeItem
	 * @return boolean
	 */
	public boolean isInComputeStack(IComputeItem obj) {
		for (LinkEntry<IComputeItem> entry = stackHead; entry != null; entry = entry.getNext()) {
			if (entry.getElement() == obj) {
				return true;
			}
		}

		return false;
	}

	/**
	 * ���ض�ջ�Ƿ��
	 * @return boolean
	 */
	public boolean isStackEmpty() {
		return stackHead == null;
	}

	public LinkEntry<IComputeItem> getStackHeadEntry() {
		return stackHead;
	}

	/**
	 * ��ռ����ջ
	 */
	public void clearStackList() {
		stackHead = null;
	}

	/**
	 * ���arg��ջ
	 */
	public void clearArgStackList() {
		argHead = null;
	}

	public void reset() {
		stackHead = null;
		argHead = null;
	}
}
