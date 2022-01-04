package com.scudata.dw.pseudo;

import com.scudata.dm.Record;
import com.scudata.dm.Sequence;

public class PseudoColumn {
	private static final String PD_NAME = "name";
	private static final String PD_EXP = "exp";
	private static final String PD_TYPE = "type";
	private static final String PD_PSEUDO = "pseudo";
	private static final String PD_ENUM = "enum";
	private static final String PD_BITS = "bits";
	private static final String PD_DIM = "dim";
	private static final String PD_FKEY = "fkey";
	
	private String name;//���ֶε�����
	private String exp;//���ֶεı��ʽ
	private String type;//��������
	private String pseudo;//α�ֶ�
	private Sequence _enum;//α�ֶζ�Ӧ��ö���б�
	private Sequence bits;//��ֵά��α�ֶ���
	private Object dim;//ָ���ά��
	private String fkey[];//����ֶ�
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	public Sequence get_enum() {
		return _enum;
	}

	public void set_enum(Sequence _enum) {
		this._enum = _enum;
	}

	public Sequence getBits() {
		return bits;
	}

	public void setBits(Sequence bits) {
		this.bits = bits;
	}

	public Object getDim() {
		return dim;
	}

	public void setDim(Object dim) {
		this.dim = dim;
	}

	public String[] getFkey() {
		return fkey;
	}

	public void setFkey(String[] fkey) {
		this.fkey = fkey;
	}
	
	public PseudoColumn(String name, String fkey[], Object dim) {
		this.name = name;
		this.fkey = fkey;
		this.dim = dim;
	}
	
	public PseudoColumn(Record rec) {
		name = (String) PseudoDefination.getFieldValue(rec, PD_NAME);
		exp = (String) PseudoDefination.getFieldValue(rec, PD_EXP);
		type = (String) PseudoDefination.getFieldValue(rec, PD_TYPE);
		pseudo = (String) PseudoDefination.getFieldValue(rec, PD_PSEUDO);
		_enum = (Sequence) PseudoDefination.getFieldValue(rec, PD_ENUM);
		bits = (Sequence) PseudoDefination.getFieldValue(rec, PD_BITS);
		dim = PseudoDefination.getFieldValue(rec, PD_DIM);
		
		Object obj = PseudoDefination.getFieldValue(rec, PD_FKEY);
		if (obj != null) {
			Sequence seq = (Sequence) obj;
			fkey = new String[seq.length()];
			seq.toArray(fkey);
		}
	}

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof PseudoColumn) {
        	PseudoColumn obj = (PseudoColumn)anObject;
            if (obj.getName() == null) return false;
            return obj.getName().equals(this.getName());
        }
        return false;
    }
}
