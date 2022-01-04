package com.scudata.dm;

import java.util.ArrayList;

/**
 * �����б���������������������߽�����
 * ������������ʱ��ѹ���������and��or��֣�������Ķ�������б��ٽ��кϲ�
 * @author WangXiaoJun
 *
 */
class Regions {
	private ArrayList<Region> list = new ArrayList<Region>();
	
	public Regions() {
	}
	
	public Regions(Region region) {
		list.add(region);
	}
	
	public ArrayList<Region> getRegionList() {
		return list;
	}
	
	/**
	 * ��˳������������������Ҳ��ص�
	 * @param region
	 */
	public void addRegion(Region region) {
		list.add(region);
	}
	
	/**
	 * ����һ�������б���������
	 * @param other �����б�
	 * @return ���������б�Ľ�
	 */
	public Regions and(Regions other) {
		Regions regions = new Regions();
		ArrayList<Region> list1 = this.list;
		ArrayList<Region> list2 = other.list;
		int size1 = list1.size();
		int size2 = list2.size();
		int i = 0, j = 0;
		
		while (i < size1 && j < size2) {
			Region r1 = list1.get(i);
			Region r2 = list2.get(j);
			if (r1.end < r2.start) {
				i++;
			} else if (r1.start > r2.end) {
				j++;
			} else {
				int start = r1.start < r2.start ? r2.start : r1.start;
				if (r1.end < r2.end) {
					i++;
					Region region = new Region(start, r1.end);
					regions.addRegion(region);
				} else if (r1.end == r2.end) {
					i++;
					j++;
					Region region = new Region(start, r1.end);
					regions.addRegion(region);
				} else {
					j++;
					Region region = new Region(start, r2.end);
					regions.addRegion(region);
				}
			}
		}
		
		return regions;
	}
	
	/**
	 * ����һ�������б���������
	 * @param other �����б�
	 * @return ���������б�Ĳ�
	 */
	public Regions or(Regions other) {
		Regions regions = new Regions();
		ArrayList<Region> list1 = this.list;
		ArrayList<Region> list2 = other.list;
		int size1 = list1.size();
		int size2 = list2.size();
		int i = 0, j = 0;
		
		while (i < size1 && j < size2) {
			Region r1 = list1.get(i);
			Region r2 = list2.get(j);
			if (r1.end < r2.start) {
				i++;
				regions.addRegion(r1);
			} else if (r1.start > r2.end) {
				j++;
				regions.addRegion(r2);
			} else {
				i++;
				j++;
				int start = r1.start < r2.start ? r1.start : r2.start;
				int end = r1.end < r2.end ? r2.end : r1.end;
				Region region = new Region(start, end);
				regions.addRegion(region);
			}
		}
		
		for (; i < size1; ++i) {
			regions.addRegion(list1.get(i));
		}
		
		for (; j < size2; ++j) {
			regions.addRegion(list2.get(j));
		}
		
		return regions;
	}
}
