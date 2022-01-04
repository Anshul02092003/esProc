package com.scudata.vdb;

import java.io.IOException;

/**
 * Ŀ¼��λ
 * @author RunQian
 *
 */
class DirZone extends Zone {
	private ISection section;
	
	public DirZone() {
	}
	
	public synchronized ISection getSection(Library library, Dir dir) {
		if (section == null && valid()) {
			if (block > 0) {
				section = ISection.read(library, block, dir);
			} else {
				 // �����ɵ���Ŀ¼����δ�ύ
				section = new Section(dir);
			}
		}
		
		return section;
	}
	
	public synchronized Section getSectionForWrite(Library library, Dir dir) {
		ISection section = getSection(library, dir);
		if (section instanceof Section) {
			return (Section)section;
		} else if (section == null) {
			return null;
		} else {
			throw ArchiveSection.getModifyException();
		}
	}
	
	public ISection getSection() {
		return section;
	}
	
	public void setSection(int block, Section section) {
		this.block = block;
		this.section = section;
	}
	
	public void releaseSection() {
		section = null;
	}
	
	// ���ܱ�ɾ�˻���������
	public boolean valid() {
		return block >= Dir.S_NORMAL;
	}
	
	public void reset(int state) {
		section = null;
		if (state != Dir.S_NORMAL) {
			block = state; // С��Dir.S_NORMAL��ʾɾ��
		} else {
			if (block < Dir.S_NORMAL) {
				block = Dir.S_NORMAL;
			}
		}
	}
	
	// Ϊ�²�������·�������׿�
	public void applySubHeader(Library library, int outerSeq, long innerSeq, Dir dir) throws IOException {
		setTxSeq(outerSeq, innerSeq);
		
		if (block == Dir.S_NORMAL) {
			block = library.applyHeaderBlock();
			Section section = (Section)getSection(library, dir);
			section.setHeader(block);
		}
	}
}
