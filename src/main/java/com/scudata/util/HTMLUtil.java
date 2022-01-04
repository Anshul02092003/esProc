package com.scudata.util;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.scudata.dm.Sequence;

/**
 * HTML�����࣬���ڶ�ȡHTML����
 * @author RunQian
 *
 */
public class HTMLUtil {
	
	/**
	 * ȡ��HTML����ָ��tag�µ�ָ����ŵ��ı������س�����
	 * @param html HTML����
	 * @param tags ��ǩ������
	 * @param seqs ��ǩ�µ��ı����
	 * @param subSeqs �����
	 * @return ����
	 */
	public static Sequence htmlparse(String html, String []tags, int []seqs, int []subSeqs) {
		int len = tags.length;
		Sequence result = new Sequence(len);
		
		Document doc = Jsoup.parse(html);
		for (int i = 0; i < len; ++i) {
			// ����tag����ȡ����Ӧ����
			Elements elements = doc.getElementsByTag(tags[i]);
			if (elements != null && elements.size() > seqs[i]) {
				// �������ȡ���ı�
				Element element = elements.get(seqs[i]);
				if (tags[i].equals("table")) {
					// ����������е�����
					Elements rows = element.select("tr");
					int rowCount = rows == null ? 0 : rows.size();
					Sequence rowValues = new Sequence(rowCount);
					result.add(rowValues);
					
					for (int r = 0; r < rowCount; ++r) {
						Element row = rows.get(r);
						Elements cols = row.select("td");
						int colCount = cols == null ? 0 : cols.size();
						if (colCount == 0 && r == 0) {
							cols = row.select("th");
							colCount = cols == null ? 0 : cols.size();
						}
						
						// ÿ�ж���һ������
						Sequence colValues = new Sequence(colCount);
						rowValues.add(colValues);
						
						for (int c = 0; c < colCount; ++c) {
							String text = cols.get(c).text();
							if (text != null) {
								colValues.add(text.trim());
							} else {
								colValues.add(null);
							}
						}
					}
				} else {
					String text = null;
					//if (subSeqs[i] > 0) {
						if (subSeqs[i] < element.childNodeSize()) {
							Node node = element.childNode(subSeqs[i]);
							text = node.toString();
						}
					//} else {
					//	text = element.text();
					//}
					
					if (text != null) {
						result.add(text.trim());
					} else {
						result.add(null);
					}
				}
			} else {
				result.add(null);
			}
		}
		
		return result;
	}

	/**
	 * ȡ����text�ڵ������
	 * @param node �ڵ�
	 * @param out �������
	 */
	private static void getAllNodeText(Node node, Sequence out) {
		if (node instanceof TextNode) {
			String text = ((TextNode)node).text();
			if (text != null) {
				text = text.trim();
				if (text.length() > 0) {
					out.add(text.trim());
				}
			}
		} else {
			// ���������ӽڵ�
			List<Node> childs = node.childNodes();
			if (childs != null) {
				for (Node child : childs) {
					getAllNodeText(child, out);
				}
			}
		}
	}
	
	/**
	 * ȡ����text�ڵ�����ݣ����س�����
	 * @param html HTML����
	 * @return ����
	 */
	public static Sequence htmlparse(String html) {
		Sequence result = new Sequence();
		Document doc = Jsoup.parse(html);
		List<Node> childs = doc.childNodes();
		
		if (childs != null) {
			for (Node child : childs) {
				getAllNodeText(child, result);
			}
		}
				
		return result;
	}
}
