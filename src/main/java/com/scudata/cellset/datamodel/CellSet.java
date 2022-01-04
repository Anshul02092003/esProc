package com.scudata.cellset.datamodel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import com.scudata.cellset.CellRefUtil;
import com.scudata.cellset.ICellSet;
import com.scudata.cellset.IColCell;
import com.scudata.cellset.INormalCell;
import com.scudata.cellset.IRowCell;
import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.ByteMap;
import com.scudata.common.CellLocation;
import com.scudata.common.Matrix;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.Sentence;
import com.scudata.dm.Context;
import com.scudata.dm.KeyWord;
import com.scudata.dm.Param;
import com.scudata.dm.ParamList;
import com.scudata.resources.EngineMessage;

/**
 * ������࣬ʵ������ɾ�е�����༭����
 * @author WangXiaoJun
 *
 */
abstract public class CellSet implements ICellSet {
	private static final long serialVersionUID = 0x02010010;

	// ɾ���еķ��ؽ��
	private static class RemoveResult {
		// ���ڻָ�ɾ������
		private Object [][]deleteRows;
		private int []deleteSeqs;
		private List<NormalCell> errorRefCells;

		public RemoveResult(Object[][] deleteRows, int[] deleteSeqs, List<NormalCell> errorRefCells) {
			this.deleteRows = deleteRows;
			this.deleteSeqs = deleteSeqs;
			this.errorRefCells = errorRefCells;
		}
	}

	protected Matrix cellMatrix;
	protected ParamList paramList; // ������

	//transient private byte[] cipherHash = new byte[16];
	transient private boolean autoAdjustExp = true;
	transient private Context ctx = new Context();

	public CellSet() {
		this(10, 10);
	}

	/**
	 * ����һ��ָ�������������ı��
	 * @param row int ����
	 * @param col int ����
	 */
	public CellSet(int row, int col) {
		if (row < 1) row = 1;
		if (col < 1) col = 1;

		cellMatrix = new Matrix(row + 1, col + 1);

		insertRowCell(1, row);
		insertColCell(1, col);
		insertCell(1, row, 1, col);
	}

	/**
	 * ������ͨ��Ԫ��
	 * @param r int
	 * @param c int
	 * @return NormalCell
	 */
	abstract public NormalCell newCell(int r, int c);

	/**
	 * �������׸�
	 * @param r int
	 * @return RowCell
	 */
	abstract public RowCell newRowCell(int r);

	/**
	 * �������׸�
	 * @param c int
	 * @return ColCell
	 */
	abstract public ColCell newColCell(int c);

	/**
	 * ȡ��ͨ��Ԫ��
	 * @param row �к�(��1��ʼ)
	 * @param col �к�(��1��ʼ)
	 * @return INormalCell
	 */
	public INormalCell getCell(int row, int col) {
		return (INormalCell) cellMatrix.get(row, col);
	}

	protected NormalCell getNormalCell(int row, int col) {
		return (NormalCell) cellMatrix.get(row, col);
	}

	/**
	 * ȡ��ͨ��Ԫ��
	 * @param id String ��Ԫ���ַ�����ʶ: B2
	 * @return INormalCell
	 */
	public INormalCell getCell(String id) {
		CellLocation cl = CellLocation.parse(id);
		if (cl != null) {
			int row = cl.getRow(), col = cl.getCol();
			if (row > 0 && row <= getRowCount() && col > 0 && col <= getColCount()) {
				return getCell(row, col);
			}
		}

		return null;
	}

	/**
	 * ����ͨ��Ԫ��
	 * @param r int �к�(��1��ʼ)
	 * @param c int �к�(��1��ʼ)
	 * @param cell INormalCell ��ͨ��Ԫ��
	 */
	public void setCell(int r, int c, INormalCell cell) {
		cell.setRow(r);
		cell.setCol(c);
		cellMatrix.set(r, c, cell);
	}

	/**
	 * ȡ���׵�Ԫ��
	 * @param r int �к�(��1��ʼ)
	 * @return IRowCell
	 */
	public IRowCell getRowCell(int r) {
		return (IRowCell) cellMatrix.get(r, 0);
	}

	/**
	 * �����׵�Ԫ��
	 * @param r int �к�(��1��ʼ)
	 * @param rc IRowCell ���׵�Ԫ��
	 */
	public void setRowCell(int r, IRowCell rc){
		rc.setRow(r);
		cellMatrix.set(r, 0, rc);
	}

	/**
	 * ȡ���׵�Ԫ��
	 * @param c int �к�(��1��ʼ)
	 * @return IColCell
	 */
	public IColCell getColCell(int c){
		return (IColCell) cellMatrix.get(0, c);
	}

	/**
	 * �����׵�Ԫ��
	 * @param c int �к�(��1��ʼ)
	 * @param cc IColCell ���׵�Ԫ��
	 */
	public void setColCell(int c, IColCell cc){
		cc.setCol(c);
		cellMatrix.set(0, c, cc);
	}

	/**
	 * @return int ���ر�������
	 */
	public int getRowCount(){
		return cellMatrix.getRowSize() - 1;
	}

	/**
	 * @return int ���ر�������
	 */
	public int getColCount(){
		return cellMatrix.getColSize() - 1;
	}

	/**
	 * ����һ��
	 * @param r �к�(��1��ʼ)
	 * @return List<NormalCell> ����ĵ�Ԫ�����ù��ɵ�����[NormalCell]
	 */
	public List<NormalCell> insertRow(int r) {
		return insertRow(r, 1);
	}

	/**
	 * �������
	 * @param r �к�(��1��ʼ)
	 * @param count ��������
	 * @return List<NormalCell> ����ĵ�Ԫ�����ù��ɵ�����[NormalCell]
	 */
	public List<NormalCell> insertRow(int r, int count) {
		List<NormalCell> errorCells = new ArrayList<NormalCell>();
		if (count < 1) {
			return errorCells;
		}

		int oldCount = getRowCount();
		if (r == oldCount + 1) {
			addRow(count);
			return errorCells;
		}

		if (r < 1 || r > oldCount) {
			return errorCells;
		}

		// �������ʽ
		int colCount = getColCount();
		if (autoAdjustExp) {
			relativeRegulate(r, count, -1, 0, oldCount, colCount, errorCells);
		}

		//������
		cellMatrix.insertRows(r, count);

		//������׸�
		insertRowCell(r, count);

		//��ӵ�Ԫ��
		insertCell(r, count, 1, colCount);

		// �������浥Ԫ����к�
		adjustRow(r + count);
		return errorCells;
	}

	/**
	 * ����һ��
	 * @param c �к�(��1��ʼ)
	 * @return List<NormalCell> ����ĵ�Ԫ�����ù��ɵ�����[NormalCell]
	 */
	public List<NormalCell> insertCol(int c) {
		return insertCol(c, 1);
	}

	/**
	 * �������
	 * @param c �к�(��1��ʼ)
	 * @param count ��������
	 * @return List<NormalCell> ����ĵ�Ԫ�����ù��ɵ�����[NormalCell]
	 */
	public List<NormalCell> insertCol(int c, int count) {
		List<NormalCell> errorCells = new ArrayList<NormalCell>();
		if (count < 1) {
			return errorCells;
		}

		int oldCount = getColCount();
		if (c == oldCount + 1) {
			addCol(count);
			return errorCells;
		}

		if (c < 1 || c > oldCount) {
			return errorCells;
		}

		// �������ʽ
		int rowCount = getRowCount();
		if (autoAdjustExp) {
			relativeRegulate( -1, 0, c, count, rowCount, oldCount, errorCells);
		}

		//������
		cellMatrix.insertCols(c, count);

		//������׸�
		insertColCell(c, count);

		//��ӵ�Ԫ��
		insertCell(1, rowCount, c, count);

		// �������浥Ԫ����к�
		adjustCol(c + count);
		return errorCells;
	}

	/**
	 * ����һ��
	 */
	public void addRow() {
		addRow(1);
	}

	/**
	 * ���Ӷ���
	 * @param count int ����
	 */
	public void addRow(int count) {
		if (count < 1) {
			return;
		}

		int rowIndex = getRowCount() + 1;
		int colCount = getColCount();

		cellMatrix.addRows(count);

		//������׸�
		insertRowCell(rowIndex, count);

		//��ӵ�Ԫ��
		insertCell(rowIndex, count, 1, colCount);
	}

	/**
	 * ����һ��
	 */
	public void addCol() {
		addCol(1);
	}

	/**
	 * ���Ӷ���
	 * @param count int ����
	 */
	public void addCol(int count) {
		if (count < 1) {
			return;
		}

		int colIndex = getColCount() + 1;
		int rowCount = getRowCount();

		cellMatrix.addCols(count);

		//������׸�
		insertColCell(colIndex, count);

		//��ӵ�Ԫ��
		insertCell(1, rowCount, colIndex, count);
	}

	/**
	 * ɾ��һ��
	 * @param r �к�(��1��ʼ)
	 * @return List<NormalCell> ����ĵ�Ԫ�����ù��ɵ�����[NormalCell]
	 */
	public List<NormalCell> removeRow(int r) {
		return removeRow(r, 1);
	}

	/**
	 * ɾ������
	 * @param r �к�(��1��ʼ)
	 * @param count ɾ������
	 * @return List<NormalCell> ����ĵ�Ԫ�����ù��ɵ�����[NormalCell]
	 */
	public List<NormalCell> removeRow(int r, int count) {
		List<NormalCell> errorCells = new ArrayList<NormalCell>();
		if (count < 1) {
			return errorCells;
		}

		int oldRowCount = getRowCount();
		int oldColCount = getColCount();

		// ɾ����
		cellMatrix.deleteRows(r, count);

		// �������ʽ
		if (autoAdjustExp) {
			relativeRegulate(r, -count, -1, 0, oldRowCount, oldColCount, errorCells);
		}

		// �������浥Ԫ����к�
		adjustRow(r);
		return errorCells;
	}

	/**
	 * ɾ��һ��
	 * @param c �к�(��1��ʼ)
	 * @return List<NormalCell> ����ĵ�Ԫ�����ù��ɵ�����[NormalCell]
	 */
	public List<NormalCell> removeCol(int c) {
		return removeCol(c, 1);
	}

	/**
	 * ɾ������
	 * @param c �к�(��1��ʼ)
	 * @param count ɾ������
	 * @return List<NormalCell> ����ĵ�Ԫ�����ù��ɵ�����[NormalCell]
	 */
	public List<NormalCell> removeCol(int c, int count) {
		List<NormalCell> errorCells = new ArrayList<NormalCell>();
		if (count < 1) {
			return errorCells;
		}

		int oldRowCount = getRowCount();
		int oldColCount = getColCount();

		//ɾ����
		cellMatrix.deleteCols(c, count);

		// �������ʽ
		if (autoAdjustExp) {
			relativeRegulate(-1, 0, c, -count, oldRowCount, oldColCount, errorCells);
		}

		// �������浥Ԫ����к�
		adjustCol(c);
		return errorCells;
	}

	/**
	 * ɾ��ָ���У��кŴ�С��������
	 * @param rows int[]
	 * @return Object ����undoRemoves
	 */
	public Object removeRows(int []rows) {
		if (rows == null || rows.length == 0) return null;
		int count = rows.length;

		List<NormalCell> errorCells = new ArrayList<NormalCell>();

		// ������ɾ������
		int colCount = getColCount();
		Object [][]deleteRows = new Object[count][];
		for (int i = 0; i < count; ++i) {
			int r = rows[i];
			Object []row = new Object[colCount + 1];
			deleteRows[i] = row;
			row[0] = getRowCell(r);
			for (int c = 1; c <= colCount; ++c) {
				row[c] = getCell(r, c);
			}
		}

		int oldRowCount = getRowCount();

		// ɾ����
		cellMatrix.deleteRows(rows);

		// �������浥Ԫ����к�
		adjustRow(rows[0]);

		// �������ʽ
		adjustRowReference(rows, false, oldRowCount, errorCells);

		RemoveResult removeResult = new RemoveResult(deleteRows, rows, errorCells);
		return removeResult;
	}

	/**
	 * ����ɾ���в���
	 * @param removeRetVal Object
	 */
	public void undoRemoveRows(Object removeRetVal) {
		if (removeRetVal == null) return;
		RemoveResult removeResult = (RemoveResult)removeRetVal;

		int []rowSeqs = removeResult.deleteSeqs;
		Object [][]rows = removeResult.deleteRows;
		List<NormalCell> errorRefCells = removeResult.errorRefCells;

		int deleteCount = rowSeqs.length;
		int []insertSeqs = new int[deleteCount];
		for (int i = 0; i < deleteCount; ++i) {
			insertSeqs[i] = rowSeqs[i] - i;
		}

		insertRows(insertSeqs, 0);
		int colCount = getColCount();
		for (int i = 0; i < deleteCount; ++i) {
			int r = rowSeqs[i];
			Object []row = rows[i];
			setRowCell(r, (IRowCell)row[0]);
			for (int c = 1; c <= colCount; ++c) {
				setCell(r, c, (INormalCell)row[c]);
			}
		}

		int size = errorRefCells == null ? 0 : errorRefCells.size();
		for (int i = 0; i < size; ++i) {
			INormalCell cell = (INormalCell)errorRefCells.get(i);
			setCell(cell.getRow(), cell.getCol(), cell);
		}
	}

	// ���������������ͨ��Ԫ��
	private void insertCell(int startRow, int rowCount, int startCol, int colCount) {
		int endRow = startRow + rowCount;
		int endCol = startCol + colCount;

		for (int r = startRow; r < endRow; ++r) {
			for (int c = startCol; c < endCol; ++c) {
				cellMatrix.set(r, c, newCell(r, c));
			}
		}
	}

	// ������׸�
	private void insertRowCell(int startRow, int rowCount) {
		int endRow = startRow + rowCount;
		for (int r = startRow; r < endRow; ++r) {
			cellMatrix.set(r, 0, newRowCell(r));
		}
	}

	// ������׸�
	private void insertColCell(int startCol, int colCount) {
		int endCol = startCol + colCount;
		for (int c = startCol; c < endCol; ++c) {
			cellMatrix.set(0, c, newColCell(c));
		}
	}

	// ����startRow����֮���еĵ�Ԫ����к�
	protected void adjustRow(int startRow) {
		int rowCount = getRowCount();
		int colCount = getColCount();

		for (int r = startRow; r <= rowCount; ++r) {
			IRowCell rowCell = getRowCell(r);
			rowCell.setRow(r);

			for (int c = 1; c <= colCount; ++c) {
				INormalCell cell = getCell(r, c);
				if (cell != null) cell.setRow(r);
			}
		}
	}

	// ����startCol����֮���еĵ�Ԫ����к�
	protected void adjustCol(int startCol) {
		int rowCount = getRowCount();
		int colCount = getColCount();

		for (int c = startCol; c <= colCount; ++c) {
			IColCell colCell = getColCell(c);
			colCell.setCol(c);

			for (int r = 1; r <= rowCount; ++r) {
				INormalCell cell = getCell(r, c);
				if (cell != null) cell.setCol(c);
			}
		}
	}

	/**
	 * ȡ����Ԫ����
	 * @return ParamList
	 */
	public ParamList getParamList(){
		return paramList;
	}

	/**
	 * �����Ԫ����
	 * @param paramList ����Ԫ����
	 */
	public void setParamList(ParamList paramList){
		this.paramList = paramList;
	}

	/**
	 * �����������������
	 * @return Context
	 */
	public Context getContext() {
		if (ctx == null) ctx = new Context();
		return ctx;
	}

	/**
	 * �����������������
	 * @param ctx Context
	 */
	public void setContext(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * ���õ�Ԫ��ֵΪ��ʼ״̬���ͷ���Դ��ɾ���������в�������ʱ������
	 * ���������ж���Ĳ������뵽�������У�������������Ѵ��ڲ����򲻱䡣
	 */
	public void reset() {
		int rowCount = getRowCount();
		int colCount = getColCount();

		for (int r = 1; r <= rowCount; ++r) {
			for (int c = 1; c <= colCount; ++c) {
				NormalCell cell = getNormalCell(r, c);
				if (cell != null) cell.reset();
			}
		}

		runFinished();
		Context ctx = getContext();
		
		if (ctx != null) {
			// ÿ�����õ�����ctx�������̱߳��ж��п��ܴ��Ҽ����ջ
			ctx.getComputeStack().reset();

			// ɾ��������
			ParamList list = ctx.getParamList();
			if (list != null) {
				list.clear();
			}
		}

		setParamToContext();
	}

	/**
	 * ��������
	 */
	abstract public void run();

	/**
	 * ִ��ĳ����Ԫ��
	 * @param row int ��Ԫ���к�
	 * @param col int ��Ԫ���к�
	 */
	abstract public void runCell(int row, int col);

	/**
	 * �������У��ͷ���Դ����ֵ�Ա���
	 */
	public void runFinished() {
	}

	/**
	 * �������ж���Ĳ����ӵ��������У�������������Ѵ��ڲ����򲻱�
	 */
	public void setParamToContext() {
		Context ctx = getContext();
		ParamList paramList = this.paramList;
		if (paramList != null) {
			for (int i = 0, count = paramList.count(); i < count; ++i) {
				Param param = paramList.get(i);
				if (ctx.getParam(param.getName()) == null) {
					// �������Ķ��Ǵ�
					Object value = param.getValue();
					ctx.setParamValue(param.getName(), value);
				}
			}
		}
	}

	public void resetParam() {
		ParamList ctxParam = new ParamList();
		ctx.setParamList(ctxParam);

		ParamList paramList = this.paramList;
		if (paramList != null) {
			for (int i = 0, count = paramList.count(); i < count; ++i) {
				Param param = paramList.get(i);

				// �������Ķ��Ǵ�
				Object value = param.getValue();
				
				// value�޸ĳɴ���ʵֵ��
				//if (value instanceof String) {
				//	value = Variant.parse((String)value);
				//}
				
				ctx.setParamValue(param.getName(), value);
			}
		}
	}

	/**
	 * д���ݵ���
	 * @param out ObjectOutput �����
	 * @throws IOException
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(1);
		out.writeObject(cellMatrix);
		out.writeObject(paramList);
	}

	/**
	 * �����ж�����
	 * @param in ObjectInput ������
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		in.readByte(); // version
		cellMatrix = (Matrix) in.readObject();
		paramList = (ParamList) in.readObject();
	}

	/**
	 * д���ݵ���
	 * @throws IOException
	 * @return �����
	 */
	public byte[] serialize() throws IOException{
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();

		// ���л���Ԫ�����
		int rowCount = getRowCount();
		int colCount = getColCount();
		out.writeInt(rowCount);
		out.writeInt(colCount);
		for (int row = 1; row <= rowCount; ++row) {
			IRowCell rc = getRowCell(row);
			out.writeRecord(rc);
		}
		for (int col = 1; col <= colCount; ++col) {
			IColCell cc = getColCell(col);
			out.writeRecord(cc);
		}
		for (int row = 1; row <= rowCount; ++row) {
			for (int col = 1; col <= colCount; ++col) {
				INormalCell nc = getCell(row, col);
				out.writeRecord(nc);
			}
		}

		out.writeRecord(paramList);
		return out.toByteArray();
	}

	/**
	 * �����ж�����
	 * @param buf byte[]
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);

		// ���ɵ�Ԫ�����
		int rowCount = in.readInt();
		int colCount = in.readInt();
		cellMatrix = new Matrix(rowCount + 1, colCount + 1);
		for (int row = 1; row <= rowCount; ++row) {
			RowCell rc = (RowCell) in.readRecord(newRowCell(row));
			cellMatrix.set(row, 0, rc);
		}
		for (int col = 1; col <= colCount; ++col) {
			ColCell cc = (ColCell)in.readRecord(newColCell(col));
			cellMatrix.set(0, col, cc);
		}
		for (int row = 1; row <= rowCount; ++row) {
			for (int col = 1; col <= colCount; ++col) {
				NormalCell nc = (NormalCell)in.readRecord(newCell(row, col));
				cellMatrix.set(row, col, nc);
			}
		}

		paramList = (ParamList)in.readRecord(new ParamList());
	}

	/**
	 * �ı䵥Ԫ����ʽ�ַ����е��кź��кţ�$���εĲ������޸ģ���$A$3��
	 * @param srcCs ICellSet ���Ƶ�Ԫ���Դ��
	 * @param cell NormalCell ���е�Ԫ����ַ������ʽ
	 * @param rowOff int �кŵ�������ֵ
	 * @param colOff int �кŵ�������ֵ
	 * @return List<NormalCell> ����ĵ�Ԫ�����ù��ɵ�����[NormalCell]
	 */
	public List<NormalCell> adjustCell(ICellSet srcCs, NormalCell cell, int rowOff, int colOff) {
		NormalCell cellClone = (NormalCell)cell.deepClone();
		boolean isErrRef = false;
		boolean []error = new boolean[1];
		List<NormalCell> errorCells = new ArrayList<NormalCell>();

		if (cell.needRegulateString()) {
			cell.setExpString(relativeRegulateString(srcCs, cell.getExpString(), rowOff, colOff, error));
			if (error[0]) isErrRef = true;
		}

		ByteMap expMap = cell.getExpMap(true);
		if (expMap != null) {
			for (int i = 0, size = expMap.size(); i < size; i++) {
				String expStr = (String)expMap.getValue(i);
				expMap.setValue(i, relativeRegulateString(srcCs, expStr, rowOff, colOff, error));
				if (error[0]) isErrRef = true;
			}

			cell.setExpMap(expMap);
		}

		if (isErrRef) {
			errorCells.add(cellClone);
		}
		
		return errorCells;
	}

	/**
	 * �����õ�Ԫ��srcLct�ı��ʽ��Ϊ���õ�Ԫ��tgtLct
	 * @param srcLct CellLocation Դλ��
	 * @param tgtLct CellLocation Ŀ��λ��
	 * @return List<NormalCell>����ĵ�Ԫ������
	 */
	public List<NormalCell> adjustReference(CellLocation srcLct, CellLocation tgtLct) {
		List<NormalCell> errorCells = new ArrayList<NormalCell>();
		if (!autoAdjustExp) {
			return errorCells;
		}
		
		if (srcLct == null || tgtLct == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("function.paramValNull"));
		}

		boolean []error = new boolean[1];
		int rowCount = getRowCount();
		int colCount = getColCount();
		for (int row = 1; row <= rowCount; row++) {
			for (int col = 1; col <= colCount; col++) {
				NormalCell cell = getNormalCell(row, col);
				if (cell != null) {
					boolean isErrRef = false;
					boolean needRegulateString = cell.needRegulateString();
					String newExpStr = null;
					
					if (needRegulateString) {
						newExpStr = relativeRegulateString(cell.getExpString(), srcLct, tgtLct, error);
						if (error[0]) isErrRef = true;
					}

					ByteMap expMap = cell.getExpMap(true);
					if (expMap != null) {
						for (int i = 0, size = expMap.size(); i < size; i++) {
							String expStr = (String)expMap.getValue(i);
							expMap.setValue(i, relativeRegulateString(expStr, srcLct, tgtLct, error));
							if (error[0]) isErrRef = true;
						}
					}
					
					if (isErrRef) {
						errorCells.add((NormalCell)cell.deepClone());
					}

					if (needRegulateString) cell.setExpString(newExpStr);
					if (expMap != null) cell.setExpMap(expMap);
				}
			}
		}
		
		return errorCells;
	}
	
	/**
	 * �ѱ��ʽ�ж�lct1�����ø�Ϊlct2�����ڰ�һ������е���һ���񣬸ı��Դ������õ�Ŀ���
	 * @param lct1 ԭ�����õĵ�Ԫ��
	 * @param lct2 Ŀ�굥Ԫ��
	 */
	public void exchangeReference(CellLocation lct1, CellLocation lct2) {
		if (!autoAdjustExp) return;

		int rowCount = getRowCount();
		int colCount = getColCount();
		for (int row = 1; row <= rowCount; row++) {
			for (int col = 1; col <= colCount; col++) {
				NormalCell cell = getNormalCell(row, col);
				if (cell != null) {
					if (cell.needRegulateString()) {
						cell.setExpString(CellRefUtil.exchangeCellString(
							cell.getExpString(), lct1, lct2));
					}

					ByteMap expMap = cell.getExpMap(false);
					if (expMap != null) {
						for (int i = 0, size = expMap.size(); i < size; i++) {
							String expStr = (String)expMap.getValue(i);
							expMap.setValue(i, CellRefUtil.exchangeCellString(expStr, lct1, lct2));
						}
					}
				}
			}
		}
	}

	/**
	 * ��������sr�ı��ʽ��Ϊ������tr
	 * @param sr int Դ��
	 * @param tr int Ŀ����
	 */
	public void adjustRowReference(int sr, int tr) {
		if (!autoAdjustExp) return;

		int rowCount = getRowCount();
		int colCount = getColCount();
		for (int row = 1; row <= rowCount; row++) {
			for (int col = 1; col <= colCount; col++) {
				NormalCell cell = getNormalCell(row, col);
				if (cell != null) {
					if (cell.needRegulateString()) {
						cell.setExpString(relativeRegulateRowString(
							cell.getExpString(), sr, tr));
					}

					ByteMap expMap = cell.getExpMap(false);
					if (expMap != null) {
						for (int i = 0, size = expMap.size(); i < size; i++) {
							String expStr = (String)expMap.getValue(i);
							expMap.setValue(i, relativeRegulateRowString(expStr, sr, tr));
						}
					}
				}
			}
		}
	}

	// ����ֵΪ��λ�õ�����Ҫ��ɵ�����,0��ʾ����
	protected void adjustRowReference(int []newSeqs) {
		if (!autoAdjustExp) return;

		int rowCount = getRowCount();
		int colCount = getColCount();
		for (int row = 1; row <= rowCount; row++) {
			for (int col = 1; col <= colCount; col++) {
				NormalCell cell = getNormalCell(row, col);
				if (cell != null) {
					if (cell.needRegulateString()) {
						cell.setExpString(relativeRegulateRowString(
							cell.getExpString(), newSeqs));
					}

					ByteMap expMap = cell.getExpMap(false);
					if (expMap != null) {
						for (int i = 0, size = expMap.size(); i < size; i++) {
							String expStr = (String)expMap.getValue(i);
							expMap.setValue(i, relativeRegulateRowString(expStr, newSeqs));
						}
					}
				}
			}
		}
	}

	// �ı��ַ����е�Ԫ����кź��кţ�$���εĲ������޸ģ���$A$3��
	// ���ظı����ַ�������("B3", 1, -1)����A4
	private String relativeRegulateString(ICellSet srcCs, String str, int rowIncrement,
										  int colIncrement, boolean []error) {
		error[0] = false;
		//��������ĵ�Ԫ�񲻴���
		if (str == null || str.length() == 0 || str.startsWith(CellRefUtil.ERRORREF)) {
			return str;
		}

		StringBuffer strNew = null;
		int len = str.length();

		for (int idx = 0; idx < len; ) {
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') { // �����ַ���
				int tmp = Sentence.scanQuotation(str, idx);
				if (tmp < 0) {
					if (strNew != null) strNew.append(str.substring(idx));
					break;
				} else {
					tmp++;
					if (strNew != null) strNew.append(str.substring(idx, tmp));
					idx = tmp;
				}
			} else if (KeyWord.isSymbol(ch) || ch == KeyWord.CELLPREFIX) {
				if (strNew != null) strNew.append(ch);
				idx++;
			} else {
				int last = scanId(str, idx);
				if (last - idx < 2 || (!CellRefUtil.isColChar(ch) && ch != '$') || 
						CellRefUtil.isPrevDot(str, idx)) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				int macroIndex = -1; // A$23��$������
				int numIndex = -1; // ���ֵ�����

				for (int i = idx + 1; i < last; ++i) {
					char tmp = str.charAt(i);
					if (tmp == '$') {
						macroIndex = i;
						numIndex = i + 1;
						break;
					} else if (CellRefUtil.isRowChar(tmp)) {
						numIndex = i;
						break;
					} else if (!CellRefUtil.isColChar(tmp)) {
						break;
					}
				}

				if (numIndex == -1) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				int rowCount = getRowCount();
				int colCount = getColCount();
				int srcRowCount = srcCs.getRowCount();
				int srcColCount = srcCs.getColCount();

				if (ch == '$') {
					if (macroIndex == -1) { // $A2
						CellLocation lct = CellLocation.parse(str.substring(idx + 1, last));

						if (lct == null || lct.getRow() > srcRowCount || lct.getCol() > srcColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strRow = CellRefUtil.changeRow(lct.getRow(), rowIncrement, rowCount);
							if (strRow == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(str.substring(idx, numIndex));
							strNew.append(strRow);
						}
					} else { // $A$2
						if (strNew != null) strNew.append(str.substring(idx, last));
					}
				} else {
					if (macroIndex == -1) { // A2
						CellLocation lct = CellLocation.parse(str.substring(idx, last));

						if (lct == null || lct.getRow() > srcRowCount || lct.getCol() > srcColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strCol = CellRefUtil.changeCol(lct.getCol(), colIncrement, colCount);
							if (strCol == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							String strRow = CellRefUtil.changeRow(lct.getRow(), rowIncrement, rowCount);
							if (strRow == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(strCol);
							strNew.append(strRow);
						}
					} else { // A$2
						int col = CellLocation.parseCol(str.substring(idx, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));
						if (col <= 0 || row <= 0 || col > srcColCount || row > srcRowCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strCol = CellRefUtil.changeCol(col, colIncrement, colCount);
							if (strCol == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(strCol);
							strNew.append(str.substring(macroIndex, last));
						}
					}
				}

				idx = last;
			}
		}

		return strNew == null ? str : strNew.toString();
	}

	// ������롢ɾ������ʱ���ݼ����ʽ�еĵ�Ԫ������
	// rowBase �����������л���
	// colBase ������ �л���
	// rowIncrement��������ɾ����������0Ϊ���룬С��0Ϊɾ��
	// colIncrement�� ��ɾ����������0Ϊ���룬С��0Ϊɾ��
	private void relativeRegulate(int rowBase, int rowIncrement, int colBase, int colIncrement, 
			int oldRowCount, int oldColCount, List<NormalCell> errorCells) {
		int rowCount = getRowCount();
		int colCount = getColCount();
		boolean []error = new boolean[1];

		for (int row = 1; row <= rowCount; row++) {
			for (int col = 1; col <= colCount; col++) {
				NormalCell cell = getNormalCell(row, col);
				if (cell != null) {
					boolean isErrRef = false;
					boolean needRegulateString = cell.needRegulateString();
					String newExpStr = null;

					if (needRegulateString) {
						newExpStr = relativeRegulateString(cell.getExpString(),
							rowBase, rowIncrement, colBase, colIncrement,
							oldRowCount, oldColCount, error);

						if (error[0]) isErrRef = true;
					}

					ByteMap expMap = cell.getExpMap(true);
					if (expMap != null) {
						for (int i = 0, size = expMap.size(); i < size; i++) {
							String expStr = (String)expMap.getValue(i);
							expMap.setValue(i, relativeRegulateString(expStr, rowBase,
								rowIncrement, colBase, colIncrement,
								oldRowCount, oldColCount, error));

							if (error[0]) isErrRef = true;
						}
					}

					if (isErrRef) {
						errorCells.add((NormalCell)cell.deepClone());
					}

					if (needRegulateString) cell.setExpString(newExpStr);
					if (expMap != null) cell.setExpMap(expMap);
				}
			}
		}
	}

	/**
	 * �ѱ��ʽ�ж�lct1�����ø�Ϊlct2�����ڰ�һ������е���һ���񣬸ı��Դ������õ�Ŀ���
	 * @param str ���ʽ
	 * @param lct1 ԭ�����õĵ�Ԫ��
	 * @param lct2 Ŀ�굥Ԫ��
	 * @return �任��ı��ʽ
	 */
	private static String relativeRegulateString(String str, int rowBase, int rowIncrement,
										  int colBase, int colIncrement,
										  int oldRowCount, int oldColCount, boolean []error) {
		error[0] = false;

		//��������ĵ�Ԫ�񲻴���
		if (str == null || str.length() == 0 || str.startsWith(CellRefUtil.ERRORREF)) {
			return str;
		}

		StringBuffer strNew = null;
		int len = str.length();

		for (int idx = 0; idx < len; ) {
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') { // �����ַ���
				int tmp = Sentence.scanQuotation(str, idx);
				if (tmp < 0) {
					if (strNew != null) strNew.append(str.substring(idx));
					break;
				} else {
					tmp++;
					if (strNew != null) strNew.append(str.substring(idx, tmp));
					idx = tmp;
				}
			} else if (KeyWord.isSymbol(ch) || ch == KeyWord.CELLPREFIX) {
				if (strNew != null) strNew.append(ch);
				idx++;
			} else {
				int last = scanId(str, idx);
				if (last - idx < 2 || (!CellRefUtil.isColChar(ch) && ch != '$') || 
						CellRefUtil.isPrevDot(str, idx)) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				int macroIndex = -1; // A$23��$������
				int numIndex = -1; // ���ֵ�����

				for (int i = idx + 1; i < last; ++i) {
					char tmp = str.charAt(i);
					if (tmp == '$') {
						macroIndex = i;
						numIndex = i + 1;
						break;
					} else if (CellRefUtil.isRowChar(tmp)) {
						numIndex = i;
						break;
					} else if (!CellRefUtil.isColChar(tmp)) {
						break;
					}
				}

				if (numIndex == -1) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				if (ch == '$') {
					if (macroIndex == -1) { // $A2
						CellLocation lct =CellLocation.parse(str.substring(idx + 1, last));
						if (lct == null || lct.getRow() > oldRowCount || lct.getCol() > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strCol = CellRefUtil.changeCol(lct.getCol(), colBase,
								colIncrement, oldColCount);
							if (strCol == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							String strRow = CellRefUtil.changeRow(lct.getRow(), rowBase,
								rowIncrement, oldRowCount);
							if (strRow == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(strCol);
							strNew.append(strRow);
						}
					} else { // $A$2
						int col = CellLocation.parseCol(str.substring(idx + 1, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));
						if (col <= 0 || row <= 0 || row > oldRowCount || col > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strCol = CellRefUtil.changeCol(col, colBase, colIncrement, oldColCount);
							if (strCol == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							String strRow = CellRefUtil.changeRow(row, rowBase, rowIncrement, oldRowCount);
							if (strRow == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(strCol);
							strNew.append('$');
							strNew.append(strRow);
						}
					}
				} else {
					if (macroIndex == -1) { // A2
						CellLocation lct = CellLocation.parse(str.substring(idx, last));
						if (lct == null || lct.getRow() > oldRowCount || lct.getCol() > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strCol = CellRefUtil.changeCol(lct.getCol(), colBase,
								colIncrement, oldColCount);
							if (strCol == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							String strRow = CellRefUtil.changeRow(lct.getRow(), rowBase,
								rowIncrement, oldRowCount);
							if (strRow == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(strCol);
							strNew.append(strRow);
						}
					} else { // A$2
						int col = CellLocation.parseCol(str.substring(idx, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));
						if (col <= 0 || row <= 0 || row > oldRowCount || col > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							String strCol = CellRefUtil.changeCol(col, colBase, colIncrement, oldColCount);
							if (strCol == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							String strRow = CellRefUtil.changeRow(row, rowBase, rowIncrement, oldRowCount);
							if (strRow == null) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							}

							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(strCol);
							strNew.append('$');
							strNew.append(strRow);
						}
					}
				}

				idx = last;
			}
		}

		return strNew == null ? str : strNew.toString();
	}

	private static String relativeRegulateString(String str, CellLocation srcLct, CellLocation tgtLct, boolean[] error) {
		error[0] = false;
		
		//��������ĵ�Ԫ�񲻴���
		if (str == null || str.length() == 0 || str.startsWith(CellRefUtil.ERRORREF)) {
			return str;
		}

		StringBuffer strNew = null;
		int len = str.length();

		for (int idx = 0; idx < len; ) {
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') { // �����ַ���
				int tmp = Sentence.scanQuotation(str, idx);
				if (tmp < 0) {
					if (strNew != null) strNew.append(str.substring(idx));
					break;
				} else {
					tmp++;
					if (strNew != null) strNew.append(str.substring(idx, tmp));
					idx = tmp;
				}
			} else if (KeyWord.isSymbol(ch) || ch == KeyWord.CELLPREFIX) {
				if (strNew != null) strNew.append(ch);
				idx++;
			} else {
				int last = scanId(str, idx);
				if (last - idx < 2 || (!CellRefUtil.isColChar(ch) && ch != '$') || 
						CellRefUtil.isPrevDot(str, idx)) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				int macroIndex = -1; // A$23��$������
				int numIndex = -1; // ���ֵ�����

				for (int i = idx + 1; i < last; ++i) {
					char tmp = str.charAt(i);
					if (tmp == '$') {
						macroIndex = i;
						numIndex = i + 1;
						break;
					} else if (CellRefUtil.isRowChar(tmp)) {
						numIndex = i;
						break;
					} else if (!CellRefUtil.isColChar(tmp)) {
						break;
					}
				}

				if (numIndex == -1) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				if (ch == '$') {
					if (macroIndex == -1) { // $A2
						CellLocation lct = CellLocation.parse(str.substring(idx + 1, last));
						if (srcLct.equals(lct)) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(tgtLct.toString());
						} else if (tgtLct.equals(lct)) {
							error[0] = true;
							return CellRefUtil.ERRORREF + str;
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					} else { // $A$2
						int col = CellLocation.parseCol(str.substring(idx + 1, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));

						if (col == srcLct.getCol() && row == srcLct.getRow()) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(CellLocation.toCol(tgtLct.getCol()));
							strNew.append('$');
							strNew.append(CellLocation.toRow(tgtLct.getRow()));
						} else if (col == tgtLct.getCol() && row == tgtLct.getRow()) {
							error[0] = true;
							return CellRefUtil.ERRORREF + str;
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					}
				} else {
					if (macroIndex == -1) { // A2
						CellLocation lct = CellLocation.parse(str.substring(idx, last));
						if (srcLct.equals(lct)) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(tgtLct.toString());
						} else if (tgtLct.equals(lct)) {
							error[0] = true;
							return CellRefUtil.ERRORREF + str;
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					} else { // A$2
						int col = CellLocation.parseCol(str.substring(idx, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));
						if (col == srcLct.getCol() && row == srcLct.getRow()) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(CellLocation.toCol(tgtLct.getCol()));
							strNew.append('$');
							strNew.append(CellLocation.toRow(tgtLct.getRow()));
						} else if (col == tgtLct.getCol() && row == tgtLct.getRow()) {
							error[0] = true;
							return CellRefUtil.ERRORREF + str;
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					}
				}

				idx = last;
			}
		}

		return strNew == null ? str : strNew.toString();
	}

	private String relativeRegulateRowString(String str, int sr, int tr) {
		//��������ĵ�Ԫ�񲻴���
		if (str == null || str.length() == 0 || str.startsWith(CellRefUtil.ERRORREF)) {
			return str;
		}

		StringBuffer strNew = null;
		int len = str.length();
		int colCount = getColCount();

		for (int idx = 0; idx < len; ) {
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') { // �����ַ���
				int tmp = Sentence.scanQuotation(str, idx);
				if (tmp < 0) {
					if (strNew != null) strNew.append(str.substring(idx));
					break;
				} else {
					tmp++;
					if (strNew != null) strNew.append(str.substring(idx, tmp));
					idx = tmp;
				}
			} else if (KeyWord.isSymbol(ch) || ch == KeyWord.CELLPREFIX) {
				if (strNew != null) strNew.append(ch);
				idx++;
			} else {
				int last = scanId(str, idx);
				if (last - idx < 2 || (!CellRefUtil.isColChar(ch) && ch != '$') || 
						CellRefUtil.isPrevDot(str, idx)) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				int macroIndex = -1; // A$23��$������
				int numIndex = -1; // ���ֵ�����

				for (int i = idx + 1; i < last; ++i) {
					char tmp = str.charAt(i);
					if (tmp == '$') {
						macroIndex = i;
						numIndex = i + 1;
						break;
					} else if (CellRefUtil.isRowChar(tmp)) {
						numIndex = i;
						break;
					} else if (!CellRefUtil.isColChar(tmp)) {
						break;
					}
				}

				if (numIndex == -1) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				if (ch == '$') {
					if (macroIndex == -1) { // $A2
						CellLocation lct = CellLocation.parse(str.substring(idx + 1, last));
						if (lct != null && lct.getRow() == sr && lct.getCol() <= colCount) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(str.substring(idx, numIndex));
							strNew.append(CellLocation.toRow(tr));
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					} else { // $A$2
						int col = CellLocation.parseCol(str.substring(idx + 1, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));

						if (col != -1 && row == sr && col <= colCount) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(str.substring(idx, numIndex));
							strNew.append(CellLocation.toRow(tr));
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					}
				} else {
					if (macroIndex == -1) { // A2
						CellLocation lct = CellLocation.parse(str.substring(idx, last));
						if (lct != null && lct.getRow() == sr && lct.getCol() <= colCount) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(str.substring(idx, numIndex));
							strNew.append(CellLocation.toRow(tr));
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					} else { // A$2
						int col = CellLocation.parseCol(str.substring(idx, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));
						if (col != -1 && row == sr && col <= colCount) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(str.substring(idx, numIndex));
							strNew.append(CellLocation.toRow(tr));
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					}
				}

				idx = last;
			}
		}

		return strNew == null ? str : strNew.toString();
	}

	private String relativeRegulateRowString(String str, int []newSeqs) {
		//��������ĵ�Ԫ�񲻴���
		if (str == null || str.length() == 0 || str.startsWith(CellRefUtil.ERRORREF)) {
			return str;
		}

		int rowCount = newSeqs.length;
		int colCount = getColCount();

		StringBuffer strNew = null;
		int len = str.length();

		for (int idx = 0; idx < len; ) {
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') { // �����ַ���
				int tmp = Sentence.scanQuotation(str, idx);
				if (tmp < 0) {
					if (strNew != null) strNew.append(str.substring(idx));
					break;
				} else {
					tmp++;
					if (strNew != null) strNew.append(str.substring(idx, tmp));
					idx = tmp;
				}
			} else if (KeyWord.isSymbol(ch) || ch == KeyWord.CELLPREFIX) {
				if (strNew != null) strNew.append(ch);
				idx++;
			} else {
				int last = scanId(str, idx);
				if (last - idx < 2 || (!CellRefUtil.isColChar(ch) && ch != '$') || 
						CellRefUtil.isPrevDot(str, idx)) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				int macroIndex = -1; // A$23��$������
				int numIndex = -1; // ���ֵ�����

				for (int i = idx + 1; i < last; ++i) {
					char tmp = str.charAt(i);
					if (tmp == '$') {
						macroIndex = i;
						numIndex = i + 1;
						break;
					} else if (CellRefUtil.isRowChar(tmp)) {
						numIndex = i;
						break;
					} else if (!CellRefUtil.isColChar(tmp)) {
						break;
					}
				}

				if (numIndex == -1) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				if (ch == '$') {
					if (macroIndex == -1) { // $A2
						CellLocation lct = CellLocation.parse(str.substring(idx + 1, last));
						int r = lct == null ? 0 : lct.getRow();
						if (r > 0 && r < rowCount && newSeqs[r] > 0 && lct.getCol() <= colCount) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(str.substring(idx, numIndex));
							strNew.append(CellLocation.toRow(newSeqs[r]));
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					} else { // $A$2
						int c = CellLocation.parseCol(str.substring(idx + 1, macroIndex));
						int r = CellLocation.parseRow(str.substring(numIndex, last));

						if (c != -1 && r > 0 && r < rowCount && newSeqs[r] > 0 && c <= colCount) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(str.substring(idx, numIndex));
							strNew.append(CellLocation.toRow(newSeqs[r]));
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					}
				} else {
					if (macroIndex == -1) { // A2  A2@cs
						CellLocation lct = CellLocation.parse(str.substring(idx, last));
						int r = lct == null ? 0 : lct.getRow();
						if (r > 0 && r < rowCount && newSeqs[r] > 0 && lct.getCol() <= colCount) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(str.substring(idx, numIndex));
							strNew.append(CellLocation.toRow(newSeqs[r]));
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					} else { // A$2  A$2@cs
						int c = CellLocation.parseCol(str.substring(idx, macroIndex));
						int r = CellLocation.parseRow(str.substring(numIndex, last));
						if (c != -1 && r > 0 && r < rowCount && newSeqs[r] > 0 && c <= colCount) {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(str.substring(idx, numIndex));
							strNew.append(CellLocation.toRow(newSeqs[r]));
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					}
				}

				idx = last;
			}
		}

		return strNew == null ? str : strNew.toString();
	}

	/**
	 * ������ɾ����ʱ�Ƿ��Զ��������ʽ
	 * @param isAuto boolean true���Զ�����
	 */
	public void setAdjustExpMode(boolean isAuto) {
		autoAdjustExp = isAuto;
	}

	/**
	 * ������ɾ����ʱ�Ƿ��Զ��������ʽ
	 * @return boolean true���Զ�����
	 */
	public boolean getAdjustExpMode() {
		return autoAdjustExp;
	}

	abstract protected void setCurrent(INormalCell cell);

	abstract protected void setParseCurrent(int row, int col);

	abstract public String getMacroReplaceString(String strCell);

	/**
	 * �����еĲ�
	 * @param r int �к�
	 * @return int
	 */
	public int getRowLevel(int r) {
		return ((RowCell)getRowCell(r)).getLevel();
	}

	/**
	 * �����еĲ�
	 * @param r int �к�
	 * @param level int ���
	 */
	public void setRowLevel(int r, int level) {
		((RowCell)getRowCell(r)).setLevel(level);
	}

	/**
	 * �����еĲ�
	 * @param c int �к�
	 * @return int
	 */
	public int getColLevel(int c) {
		return ((ColCell)getColCell(c)).getLevel();
	}

	/**
	 * �����еĲ�
	 * @param c int �к�
	 * @param level int ���
	 */
	public void setColLevel(int c, int level) {
		((ColCell)getColCell(c)).setLevel(level);
	}

	/**
	 * ������ת��
	 * @return List<NormalCell> ����ĵ�Ԫ�����ù��ɵ�����[NormalCell]
	 */
	public List<NormalCell> transpose() {
		Matrix cellMatrix = this.cellMatrix;
		int rowSize = cellMatrix.getRowSize();
		int colSize = cellMatrix.getColSize();
		int oldRowCount = rowSize - 1;
		int oldColCount = colSize - 1;

		Matrix newMatrix = new Matrix(colSize, rowSize); // ת��
		this.cellMatrix = newMatrix;

		// �������׸�
		for (int r = 1; r < colSize; ++r) {
			ColCell cc = (ColCell)cellMatrix.get(0, r);
			RowCell rc = newRowCell(r);

			// ��ԭ���е������赽ת�ú����������
			//rc.setHeight(cc.getWidth()); // ʹ��ȱʡֵ
			rc.setLevel(cc.getLevel());
			newMatrix.set(r, 0, rc);
		}

		// �������׸�
		for (int c = 1; c < rowSize; ++c) {
			RowCell rc = (RowCell)cellMatrix.get(c, 0);
			ColCell cc = newColCell(c);

			// ��ԭ���е������赽ת�ú����������
			//cc.setWidth(rc.getHeight()); // ʹ��ȱʡֵ
			cc.setLevel(rc.getLevel());
			newMatrix.set(0, c, cc);
		}

		// �޸���ͨ��Ԫ�����к�
		for (int r = 1; r < colSize; ++r) {
			for (int c = 1; c < rowSize; ++c) {
				NormalCell cell = (NormalCell)cellMatrix.get(c, r);
				if (cell != null) {
					cell.setRow(r);
					cell.setCol(c);
					newMatrix.set(r, c, cell);
				}
			}
		}

		// �޸ı��ʽ����
		List<NormalCell> errorCells = new ArrayList<NormalCell>();
		if (getAdjustExpMode()) {
			transposeCellString(errorCells, oldRowCount, oldColCount);
			for (int i = 0, size = errorCells.size(); i < size; ++i) {
				NormalCell cell = (NormalCell)errorCells.get(i);
				int r = cell.getRow();
				cell.setRow(cell.getCol());
				cell.setCol(r);
			}
		}

		return errorCells;
	}

	private void transposeCellString(List<NormalCell> errorCells, int oldRowCount, int oldColCount) {
		int rowCount = getRowCount();
		int colCount = getColCount();
		boolean []error = new boolean[1];

		for (int row = 1; row <= rowCount; row++) {
			for (int col = 1; col <= colCount; col++) {
				NormalCell cell = getNormalCell(row, col);
				if (cell != null) {
					boolean isErrRef = false;
					boolean needRegulateString = cell.needRegulateString();
					String newExpStr = null;

					if (needRegulateString) {
						newExpStr = transposeCellString(cell.getExpString(), error, oldRowCount, oldColCount);
						if (error[0]) isErrRef = true;
					}

					ByteMap expMap = cell.getExpMap(true);
					if (expMap != null) {
						for (int i = 0, size = expMap.size(); i < size; i++) {
							String expStr = (String)expMap.getValue(i);
							expMap.setValue(i, transposeCellString(expStr, error, oldRowCount, oldColCount));
							if (error[0]) isErrRef = true;
						}
					}

					if (isErrRef) {
						errorCells.add((NormalCell)cell.deepClone());
					}

					if (needRegulateString) cell.setExpString(newExpStr);
					if (expMap != null) cell.setExpMap(expMap);
				}
			}
		}
	}

	private String transposeCellString(String str, boolean[] error, int oldRowCount, int oldColCount) {
		error[0] = false;
		//��������ĵ�Ԫ�񲻴���
		if (str == null || str.length() == 0 || str.startsWith(CellRefUtil.ERRORREF)) {
			return str;
		}

		StringBuffer strNew = null;
		int len = str.length();

		for (int idx = 0; idx < len; ) {
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') { // �����ַ���
				int tmp = Sentence.scanQuotation(str, idx);
				if (tmp < 0) {
					if (strNew != null) strNew.append(str.substring(idx));
					break;
				} else {
					tmp++;
					if (strNew != null) strNew.append(str.substring(idx, tmp));
					idx = tmp;
				}
			} else if (KeyWord.isSymbol(ch) || ch == KeyWord.CELLPREFIX) {
				if (strNew != null) strNew.append(ch);
				idx++;
			} else {
				int last = KeyWord.scanId(str, idx);
				if (last - idx < 2 || (!CellRefUtil.isColChar(ch) && ch != '$') || 
						CellRefUtil.isPrevDot(str, idx)) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				int macroIndex = -1; // A$23��$������
				int numIndex = -1; // ���ֵ�����

				for (int i = idx + 1; i < last; ++i) {
					char tmp = str.charAt(i);
					if (tmp == '$') {
						macroIndex = i;
						numIndex = i + 1;
						break;
					} else if (CellRefUtil.isRowChar(tmp)) {
						numIndex = i;
						break;
					} else if (!CellRefUtil.isColChar(tmp)) {
						break;
					}
				}

				if (numIndex == -1) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				if (ch == '$') {
					if (macroIndex == -1) { // $A2
						CellLocation lct = CellLocation.parse(str.substring(idx + 1, last));

						if (lct == null || lct.getRow() > oldRowCount || lct.getCol() > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(CellLocation.toCol(lct.getRow()));
							strNew.append(CellLocation.toRow(lct.getCol()));
						}
					} else { // $A$2
						int col = CellLocation.parseCol(str.substring(idx + 1, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));
						if (col == -1 || row == -1 || row > oldRowCount || col > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append('$');
							strNew.append(CellLocation.toCol(row));
							strNew.append('$');
							strNew.append(CellLocation.toRow(col));
						}
					}
				} else {
					if (macroIndex == -1) { // A2  A2@cs
						CellLocation lct = CellLocation.parse(str.substring(idx, last));
						if (lct == null || lct.getRow() > oldRowCount || lct.getCol() > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(CellLocation.toCol(lct.getRow()));
							strNew.append(CellLocation.toRow(lct.getCol()));
						}
					} else { // A$2  A$2@cs
						int col = CellLocation.parseCol(str.substring(idx, macroIndex));
						int row = CellLocation.parseRow(str.substring(numIndex, last));
						if (col == -1 || row == -1 || row > oldRowCount || col > oldColCount) {
							if (strNew != null) strNew.append(str.substring(idx, last));
						} else {
							if (strNew == null) {
								strNew = new StringBuffer(64);
								strNew.append(str.substring(0, idx));
							}

							strNew.append(CellLocation.toCol(row));
							strNew.append('$');
							strNew.append(CellLocation.toRow(col));
						}
					}
				}

				idx = last;
			}
		}

		return strNew == null ? str : strNew.toString();
	}

	// rows �����е���������Ŵ�С��������
	protected void insertRows(int []rows, int level) {
		int count = rows.length;
		if (count == 0) return;

		int oldRowCount = getRowCount();
		List<NormalCell> errorCells = new ArrayList<NormalCell>();
		adjustRowReference(rows, true, oldRowCount, errorCells);

		//������
		cellMatrix.insertRows(rows);

		int colCount = getColCount();
		for (int i = 0; i < count; ++i) {
			int r = rows[i] + i;

			//������׸�
			insertRowCell(r, 1);

			//��ӵ�Ԫ��
			insertCell(r, 1, 1, colCount);

			setRowLevel(r, level);
		}

		// �������浥Ԫ����к�
		adjustRow(rows[0]);
	}

	// rows �����ɾ���е���������Ŵ�С��������
	private void adjustRowReference(int[] rows, boolean isInsert, int oldRowCount, List<NormalCell> errorCells) {
		int rowCount = getRowCount();
		int colCount = getColCount();
		boolean []error = new boolean[1];

		for (int row = 1; row <= rowCount; row++) {
			for (int col = 1; col <= colCount; col++) {
				NormalCell cell = getNormalCell(row, col);
				if (cell != null) {
					boolean isErrRef = false;
					boolean needRegulateString = cell.needRegulateString();
					String newExpStr = null;
					if (needRegulateString) {
						newExpStr = relativeRegulateRowString(cell.getExpString(),
							rows, isInsert, oldRowCount, error);
						if (error[0]) isErrRef = true;
					}

					ByteMap expMap = cell.getExpMap(true);
					if (expMap != null) {
						for (int i = 0, size = expMap.size(); i < size; i++) {
							String expStr = (String)expMap.getValue(i);
							expMap.setValue(i, relativeRegulateRowString(expStr, rows,
								isInsert, oldRowCount, error));

							if (error[0])isErrRef = true;
						}
					}

					if (isErrRef) {
						errorCells.add((NormalCell)cell.deepClone());
					}

					if (needRegulateString) cell.setExpString(newExpStr);
					if (expMap != null) cell.setExpMap(expMap);
				}
			}
		}
	}

	// ��ɾ��ʱ�޸ĵ�Ԫ������
	private String relativeRegulateRowString(String str, int[] rows,  boolean isInsert, int oldRowCount, boolean[] error) {
		error[0] = false;
		//��������ĵ�Ԫ�񲻴���
		if (str == null || str.length() == 0 || str.startsWith(CellRefUtil.ERRORREF)) {
			return str;
		}

		int oldColCount = getColCount();
		StringBuffer strNew = null;
		int len = str.length();

		for (int idx = 0; idx < len; ) {
			char ch = str.charAt(idx);
			if (ch == '\'' || ch == '\"') { // �����ַ���
				int tmp = Sentence.scanQuotation(str, idx);
				if (tmp < 0) {
					if (strNew != null) strNew.append(str.substring(idx));
					break;
				} else {
					tmp++;
					if (strNew != null) strNew.append(str.substring(idx, tmp));
					idx = tmp;
				}
			} else if (KeyWord.isSymbol(ch) || ch == KeyWord.CELLPREFIX) {
				if (strNew != null) strNew.append(ch);
				idx++;
			} else {
				int last = scanId(str, idx);
				if (last - idx < 2 || (!CellRefUtil.isColChar(ch) && ch != '$') || 
						CellRefUtil.isPrevDot(str, idx)) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				int macroIndex = -1; // A$23��$������
				int numIndex = -1; // ���ֵ�����

				for (int i = idx + 1; i < last; ++i) {
					char tmp = str.charAt(i);
					if (tmp == '$') {
						macroIndex = i;
						numIndex = i + 1;
						break;
					} else if (CellRefUtil.isRowChar(tmp)) {
						numIndex = i;
						break;
					} else if (!CellRefUtil.isColChar(tmp)) {
						break;
					}
				}

				if (numIndex == -1) {
					if (strNew != null) strNew.append(str.substring(idx, last));
					idx = last;
					continue;
				}

				if (ch == '$') {
					if (macroIndex == -1) { // $A2
						CellLocation lct = CellLocation.parse(str.substring(idx + 1, last));
						if (lct != null && lct.getRow() <= oldRowCount && lct.getCol() <= oldColCount) {
							int r = lct.getRow();
							int nr = CellRefUtil.adjustRowReference(r, rows, isInsert);
							if (nr < 0) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							} else if (nr != r) {
								if (strNew == null) {
									strNew = new StringBuffer(64);
									strNew.append(str.substring(0, idx));
								}

								strNew.append(str.substring(idx, numIndex));
								strNew.append(CellLocation.toRow(nr));
							} else {
								if (strNew != null) strNew.append(str.substring(idx, last));
							}
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					} else { // $A$2
						int c = CellLocation.parseCol(str.substring(idx + 1, macroIndex));
						int r = CellLocation.parseRow(str.substring(numIndex, last));

						if (c > 0 && r > 0 && c <= oldColCount && r <= oldRowCount) {
							int nr = CellRefUtil.adjustRowReference(r, rows, isInsert);
							if (nr < 0) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							} else if (nr != r) {
								if (strNew == null) {
									strNew = new StringBuffer(64);
									strNew.append(str.substring(0, idx));
								}

								strNew.append(str.substring(idx, numIndex));
								strNew.append(CellLocation.toRow(nr));
							} else {
								if (strNew != null) strNew.append(str.substring(idx, last));
							}
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					}
				} else {
					if (macroIndex == -1) { // A2
						CellLocation lct = CellLocation.parse(str.substring(idx, last));
						if (lct != null && lct.getRow() <= oldRowCount && lct.getCol() <= oldColCount) {
							int r = lct.getRow();
							int nr = CellRefUtil.adjustRowReference(r, rows, isInsert);
							if (nr < 0) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							} else if (nr != r) {
								if (strNew == null) {
									strNew = new StringBuffer(64);
									strNew.append(str.substring(0, idx));
								}

								strNew.append(str.substring(idx, numIndex));
								strNew.append(CellLocation.toRow(nr));
							} else {
								if (strNew != null) strNew.append(str.substring(idx, last));
							}
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					} else { // A$2
						int c = CellLocation.parseCol(str.substring(idx, macroIndex));
						int r = CellLocation.parseRow(str.substring(numIndex, last));
						if (c > 0 && r > 0 && c <= oldColCount && r <= oldRowCount) {
							int nr = CellRefUtil.adjustRowReference(r, rows, isInsert);
							if (nr < 0) {
								error[0] = true;
								return CellRefUtil.ERRORREF + str;
							} else if (nr != r) {
								if (strNew == null) {
									strNew = new StringBuffer(64);
									strNew.append(str.substring(0, idx));
								}

								strNew.append(str.substring(idx, numIndex));
								strNew.append(CellLocation.toRow(nr));
							} else {
								if (strNew != null) strNew.append(str.substring(idx, last));
							}
						} else {
							if (strNew != null) strNew.append(str.substring(idx, last));
						}
					}
				}

				idx = last;
			}
		}

		return strNew == null ? str : strNew.toString();
	}
	
	public static int scanId(String expStr, int start) {
		int len = expStr.length();
		char c = expStr.charAt(start);
		if ((c >= 0x0001) && (c <= 0x007F)) {
			start++;
		} else {
			// ����Ǻ�������һ�������֣������Ϳ��Ա�Ǩע��������ֵĵ�Ԫ��
			// ����Ҫ���ֺ�������һ��char��ʾ����������char��ʾ
			return start + 1;
		}
		
		for (; start < len; start++) {
			c = expStr.charAt(start);
			if (KeyWord.isSymbol(c) || c < 0x0001 || c > 0x007F) {
				break;
			}
		}

		return start;
	}
}
