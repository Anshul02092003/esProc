package com.scudata.expression;

import com.scudata.dm.Canvas;

/**
 * Canvas.f() ������Ա������Ҫ�̳д���
 * @author Joancy
 *
 */
public abstract class CanvasFunction extends MemberFunction {
	protected Canvas canvas; // ����
	
	public boolean isLeftTypeMatch(Object obj) {
		return obj instanceof Canvas;
	}
	
	public void setDotLeftObject(Object obj) {
		canvas = (Canvas)obj;
	}
}
