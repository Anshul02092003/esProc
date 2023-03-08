package com.scudata.lib.elastic.function;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.scudata.common.Logger;
import com.scudata.common.RQException;
import com.scudata.dm.Sequence;
import com.scudata.expression.IParam;
import com.scudata.util.JSONUtil;

import de.siegmar.fastcsv.writer.CsvWriter;
import net.sf.json.JSONObject;

// export(fp, fileName[:cs], index, [cols,...])
public class ImExport  extends ImFunction {	 
	
	int m_pageNum = 10000;
	String m_charset = "gbk";
	RestHighLevelClient m_client = null;
	
	private void init(RestClientBuilder restClient) {
		m_client = new RestHighLevelClient(restClient);
	}
	
	public Object doQuery(Object[] objs) {
		try {
			if (objs.length < 2){
				throw new RQException("es_export function.paramTypeError");
			}
			init(m_restConn.m_clientBuilder);
			String fileName = null;
			String indexName = null;
			String cols[] = null;
			if (objs[0] instanceof String) {
				fileName = objs[0].toString();
			}else if (objs[0] instanceof IParam) {
				IParam pp = (IParam)objs[0];
				fileName = pp.getSub(0).getLeafExpression().calculate(m_ctx).toString();
				m_charset = pp.getSub(1).getLeafExpression().calculate(m_ctx).toString();
			}
			if (objs[1] instanceof String) {
				indexName = objs[1].toString();
			}
			if (objs.length>2 && objs[2] instanceof Sequence) {
				Sequence seq = (Sequence)objs[2];
				cols = new String[seq.length()];
				for(int i=0; i<seq.length(); i++) {
					cols[i] = seq.get(i+1).toString();
				}
			}
			
			return exportIndex(fileName, indexName, cols);			
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		return null;
	}
	
	public boolean exportIndex(String fileName, String indexName, String[] cols) throws IOException {
		boolean bRet = false;
		try {
			if (m_client==null) {
				m_client = new RestHighLevelClient(RestClient.builder(
						 new HttpHost("192.168.0.11", 9200, "http")));
				m_colMap = new LinkedHashMap<String, Integer>(); 
			}
			boolean bCsv = false;
			if (fileName.toLowerCase().endsWith("csv")) {
				bCsv = true;
			}
			
	        String linefeed="\r\n";
	        String osName = System.getProperties().getProperty("os.name");
	        if (osName.equalsIgnoreCase("Linux")) {
	        	linefeed="\n";
	        }else if (osName.equalsIgnoreCase("Mac")) {
	            linefeed="\r";
	        } else {
	            ;
	        }
	        
	        // �趨����ʱ����
	        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(2L));
	        // ��ѯȫ��
	        SearchRequest searchRequest = new SearchRequest(indexName);
	        searchRequest.scroll(scroll);
	        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
	        // ÿ�η���10000������
	        searchSourceBuilder.size(m_pageNum);
	        // �����������ֶ���Ϣ(�����д��Ĭ�ϱ���ȫ����������Ҫ����ȫ����д�Ļ����Ǳ��������е��ֶ�)
	        if (cols!=null) {
	        	searchSourceBuilder.fetchSource(cols, new  String[] {});
	        }
	
	        // bool��ѯ
	        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
	        searchSourceBuilder.query(boolBuilder);
	        searchRequest.source(searchSourceBuilder);
	        SearchResponse searchResponse = m_client.search(searchRequest, RequestOptions.DEFAULT);
	        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
	        
	        CsvWriter csvWriter = null;
	        BufferedWriter jsonWrite = null;
	        if (bCsv) {
				final Path path = new File(fileName).toPath();
				csvWriter = CsvWriter.builder().build(path, Charset.forName(m_charset));
	        }else {
	        	//out = new BufferedWriter(new FileWriter(fileName, true));
	        	jsonWrite = new BufferedWriter(new OutputStreamWriter(
	        			new FileOutputStream(new File(fileName)), m_charset));
	        }
	        // ����id
	        String scrollId = searchResponse.getScrollId();
	        SearchHit[] hits = hits = searchResponse.getHits().getHits();	        
	        do {
	        	if (bCsv) {
	        		csvWrite(csvWriter, hits, linefeed);
	        	} else {
	        		jsonWrite(jsonWrite, hits, linefeed);	
	        	}
	        	SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                // �ٴ����ù���ʱ��
                scrollRequest.scroll(scroll);
                // ִ�й�����ѯ
                searchResponse = m_client.scroll(scrollRequest, RequestOptions.DEFAULT);
                // ��ȡ����id
                scrollId = searchResponse.getScrollId();
                // �������
                clearScrollRequest.addScrollId(scrollId);
                // ��������
                hits = searchResponse.getHits().getHits();		       
		    }while (Objects.nonNull(hits) && hits.length > 0) ;

            // ���ִ�������crollIdʹ����֮���ֶ�����������ڴ�ʹ��������ȻҲ���Բ�����es���Զ�����ģ�����û���ֶ������
            ClearScrollResponse clearScrollResponse = m_client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            bRet = clearScrollResponse.isSucceeded();
            
            if (bCsv && csvWriter!=null) {
            	csvWriter.close();
	        }else if(jsonWrite!=null) {
	        	jsonWrite.close();
	        }
        } catch (Exception e) {
        	Logger.error(e.getMessage());
        } finally {
        	if(m_client!=null) {
        		m_client.close();
        	}
        }
		
		return bRet;
    }
	
	// ��json��ʽ�����ݵ������ļ��У��쳣һ�е�Щ
	private void jsonWrite(BufferedWriter out, SearchHit[] hits, String linefeed) {
		try {
            for (SearchHit hit : hits) {
                // ��ȡΪString
                String json = hit.getSourceAsString();
                out.write(json);
                out.write(linefeed);		                
            }
            out.flush();
        }catch(Exception e) {
        	Logger.error(e.getMessage());
        }
	}
	
	private void csvWrite(CsvWriter csv, SearchHit[] hits, String linefeed) {
		try {
			List<String> lines = new ArrayList<String>();
			boolean bTitle = true;
            for (SearchHit hit : hits) {
                // ��ȡΪString
                String json = hit.getSourceAsString();
                JSONObject jobs = JSONObject.fromString(json);  // ���� jsonarray ���飬��ÿһ������ת�� json ����
                doParseJoson(jobs, lines, bTitle);
                if (bTitle) {
                	bTitle = false;
                	csv.writeRow(m_colMap.keySet());
                }
                csv.writeRow(lines);  	
                lines.clear();
            }
        }catch(Exception e) {
        	e.printStackTrace();
        }
	}
	
	protected void doParseJoson(JSONObject job, List<String> ls, boolean bTitle){
		Iterator iterator = job.keys();

		// ÿ�������е�����ֵ
		int n = 0;
		if (bTitle) {
			m_colMap.clear();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				String value = job.getString(key);
				m_colMap.put(key, n);
				if (ImUtils.isJson(value)){
					ls.add( JSONUtil.parseJSON(value.toCharArray(), 0, value.length()-1).toString());
				}else{
					ls.add( value );
				}
			}
		}else{
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				String value = job.getString(key);	
				
				if ( !m_colMap.containsKey(key)){
					n = m_colMap.size();
					m_colMap.put(key, n);
				}else{
					n =m_colMap.get(key);
				}
				if (ImUtils.isJson(value)){
					ls.add( JSONUtil.parseJSON(value.toCharArray(), 0, value.length()-1).toString() );
				}else{
					ls.add( value );
				}
			}
		}
	}
	
	public static void main(String[] args) {
		boolean bRet = false;
		ImExport cls = new ImExport();
		try {
			// bRet = cls.getEs("2021-01-01", "2022-07-01");
			cls.exportIndex("d:/tmp/shops1001.csv","shops",null);
			System.out.println(bRet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
