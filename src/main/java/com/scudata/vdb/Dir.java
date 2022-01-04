package com.scudata.vdb;

import java.io.IOException;
import java.util.ArrayList;

import com.scudata.dm.ObjectReader;
import com.scudata.dm.ObjectWriter;

/**
 * Ŀ¼
 * @author RunQian
 *
 */
class Dir extends IDir {
	public static final int S_NORMAL = 0;
	public static final int S_DELETE = -1;
	public static final int S_MOVE = -2;
	
	private ArrayList<DirZone> zones; // ��λ�б�
	private transient Section parent; // ����
	
	public Dir(Section parent) {
		this.parent = parent;
	}
	
	public Dir(Object value, String name, Section parent) {
		this.value = value;
		this.name = name;
		this.parent = parent;
		
		zones = new ArrayList<DirZone>(1);
		DirZone zone = new DirZone();
		zones.add(zone);
	}
		
	public void read(ObjectReader reader) throws IOException {
		value = reader.readObject();
		name = (String)reader.readObject();
		
		int count = reader.readInt();
		zones = new ArrayList<DirZone>(count);
		for (int i = 0; i < count; ++i) {
			DirZone zone = new DirZone();
			zone.read(reader);
			zones.add(zone);
		}
	}
	
	public void write(ObjectWriter writer, Library library, int outerSeq, long innerSeq) throws IOException {
		writer.writeObject(value);
		writer.writeObject(name);
		
		int count = zones.size();
		writer.writeInt(count);
		
		DirZone lastZone = zones.get(count - 1);
		if (!lastZone.isCommitted()) {
			synchronized(this) {
				lastZone.applySubHeader(library, outerSeq, innerSeq, this);
			}
		}
		
		for (DirZone zone : zones) {
			zone.write(writer);
		}
	}
	
	public void write(ObjectWriter writer) throws IOException {
		writer.writeObject(value);
		writer.writeObject(name);
		
		int count = zones.size();
		if (count > 0) {
			DirZone lastZone = zones.get(count - 1);
			writer.writeInt(1);
			lastZone.write(writer);
		} else {
			writer.writeInt(0);
		}
	}
	
	public Section getParentSection() {
		return parent;
	}
	
	public ISection getParent() {
		return parent;
	}
	
	public synchronized DirZone addZone(int state) {
		if (zones == null) {
			zones = new ArrayList<DirZone>(1);
		}/* else {
			// ����Ѿ���һ����û�ύ����λ�򸲸�
			int size = zones.size();
			if (size > 0) {
				DirZone zone = zones.get(size - 1);
				if (!zone.isCommitted()) {
					zone.reset(state);
					return zone;
				}
			}
		}*/ // move�������ڸ���
		
		DirZone zone = new DirZone();
		zone.setBlock(state);
		
		zones.add(zone);
		return zone;
	}
	
	// ������λ��
	public synchronized int roolBack() {
		for (int i = zones.size() - 1; i >= 0; --i) {
			DirZone zone = zones.get(i);
			if (!zone.isCommitted()) {
				zones.remove(i);
			} else {
				return i + 1;
			}
		}

		return 0;
	}
	
	// �ж�ָ���߼����ܷ񿴵���Ŀ¼path
	public synchronized DirZone getZone(VDB vdb, boolean isLockVDB) {
		ArrayList<DirZone> zones = this.zones;
		if (zones == null) return null;
		
		for (int i = zones.size() - 1; i >= 0; --i) {
			DirZone zone = zones.get(i);
			if (zone.match(vdb, isLockVDB)) {
				return zone.valid() ? zone : null;
			}
		}
		
		return null;
	}
	
	public synchronized DirZone getLastZone() {
		if (zones == null) return null;
		
		int size = zones.size();
		if (size > 0) {
			return zones.get(size - 1);
		} else {
			return null;
		}
	}
	
	// ɾ���������txSeq��Ķ������λ
	public synchronized void deleteOutdatedZone(Library library, int outerSeq, long txSeq) {
		// ���ٱ���һ�����ύ����λ�����ڳ���ʱ�ָ���
		ArrayList<DirZone> zones = this.zones;
		int last = zones.size() - 1;
		if (last < 1) {
			return;
		}
		
		for (; last >= 0; --last) {
			DirZone zone = zones.get(last);
			if (zone.isCommitted()) {
				break;
			}
		}
		
		for (--last; last >= 0; --last) {
			DirZone zone = zones.get(last);
			if (zone.canDelete(outerSeq, txSeq)) {
				for (; last >= 0; --last) {
					zones.remove(last);
				}
				
				break;
			}
		}
	}
	
	// �������ݿ���׼��������ɾ������λ��ȡ��ռ�õ������
	public boolean scanUsedBlocks(Library library, BlockManager manager) throws IOException {
		ArrayList<DirZone> zones = this.zones;
		int size = zones.size();
		DirZone zone;
		
		if (size > 1) {
			zone = zones.get(size - 1);
			if (!zone.valid()) {
				return false;
			}
			
			zones.clear();
			zones.add(zone);
		} else {
			zone = zones.get(0);
			if (!zone.valid()) {
				return false;
			}
		}
		
		ISection section = zone.getSection(library, this);
		section.scanUsedBlocks(library, manager);
		zone.releaseSection();
		
		return true;
	}
	
	public void rename(String newName) {
		name = newName;
	}
	
	public void releaseSubSection() {
		if (zones != null) {
			for (DirZone zone : zones) {
				zone.releaseSection();
			}
		}
	}
}
