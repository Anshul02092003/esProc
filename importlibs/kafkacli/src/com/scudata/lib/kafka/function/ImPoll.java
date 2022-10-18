package com.scudata.lib.kafka.function;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import com.scudata.common.Logger;
import com.scudata.dm.Param;
import com.scudata.dm.Sequence;

/*
 * ImPoll@e(fd, timeout, [p1,p2,...])
 */
public class ImPoll extends ImFunction {
	private Set<TopicPartition> m_topicPartition;		// ָ��λ�ý�������
	
	@SuppressWarnings("unchecked")
	public Object doQuery(Object[] objs) {
		try {			
			boolean bCursor = false;
			if (m_conn.m_bClose) return null;
			if (option!=null){
				if (option.indexOf("c")!=-1){
					bCursor = true;
				}
			}

			long timeout = 1000;
			if (objs!=null){
				timeout = Integer.parseInt(objs[0].toString());
			}
			
			List<Integer> partitions = new ArrayList<Integer>();
			if (objs!=null && objs.length>1){
				if(objs[1] instanceof Integer){
					partitions.add((Integer)objs[1]);
				}else if(objs[1] instanceof Sequence){
					Sequence seq = (Sequence)objs[1];
					for(int i=0; i<seq.length(); i++){
						partitions.add((Integer)seq.get(i+1));
					}
				}
			}
	        
			//�ٴε���pollʱ���ٳ�ʼ��.
			Param param = null;
			boolean bPollRun = false;
			param = m_ctx.getParam("poll_run");
			if (param!=null){
				bPollRun = (Boolean)param.getValue(); 
			}
			
			int nPartSize = partitions.size();
			
			/********************************
			 * ����seek([p1,p2...], off)�󣬶�������poll()ֻ�ܵõ���λ�ķ������ݣ���˲������淽ʽ:
			 * ������offset������poll��parti����ʱ,��ȡ���еķ���
			 * Ҳ������offset����ʱ������poll��������������.
			 * 
			 **********************************/
			param = m_ctx.getParam("offset_val");
			if (nPartSize==0 && param!=null){
				partitions = getPaititionSize();
				nPartSize = partitions.size();
			}

			if (nPartSize>0){
				//�����޸ĺ������³�ʼ��������ǰ������Ƿ����ѹ���
				param = m_ctx.getParam("partition");
				if (param!=null){
					List<Integer> oldPorts = (List<Integer>)param.getValue(); 
					if (!oldPorts.equals(partitions)){
						bPollRun = false;
					}
				}
				
				if (!bPollRun){
					m_conn.initConsumerCluster(partitions);
					setComsumerOffset(partitions);
				}
				m_ctx.setParamValue("partition", partitions);
			}else{
				param = m_ctx.getParam("partition");
				if (param!=null){
					bPollRun = false;
					m_ctx.setParamValue("partition", null);
				}
				
				if (!bPollRun){
					m_conn.initConsumer();
				}
			}
			
			m_ctx.setParamValue("poll_run", true);
			if (bCursor){
				return new ImCursor(m_ctx, m_conn, timeout, nPartSize);
			}else{
				List<Object[]> ls = null;
				if (nPartSize==0 && !bPollRun){
					partitions = getPaititionSize();
					nPartSize = partitions.size();
				}
				ls = ImFunction.getClusterData(m_conn, timeout, nPartSize);
				
		        return ImFunction.toTable(m_conn.m_cols, ls);
			}		
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		
		return null;
	}
	
	private List<Integer> getPaititionSize(){
		KafkaConsumer<Object, Object> consumer = 
				new KafkaConsumer<Object, Object>(m_conn.getProperties());
		consumer.subscribe(Arrays.asList(m_conn.getTopic()));
		// ָ��λ�ý�������
		m_topicPartition = consumer.assignment();

        // ��֤�������䷽���Ѿ��ƶ����
		// �����жϿ����Ǹ���ѭ����ѭ���������˳���
		int nCount = 0;
        while (m_topicPartition.size() == 0){
        	consumer.poll(Duration.ofSeconds(1));
        	m_topicPartition = consumer.assignment();
        	if (nCount++ > 10){
        		break;
        	}
        }
        List<Integer> partitions = new ArrayList<Integer>();
		for(TopicPartition t:m_topicPartition){
			partitions.add(t.partition());
		}
		
		consumer.unsubscribe();
		consumer.close();
		
		return partitions;
	}
	
	// �Բ��������������ߵ�offset��������
	@SuppressWarnings("unchecked")
	private void setComsumerOffset(){		
		Param param = m_ctx.getParam("offset_val");
		if (param!=null){
			if (param.getValue() instanceof Map){
				Map<Integer, Integer> map = (Map<Integer, Integer>)param.getValue();
				for (TopicPartition topicPartition : m_topicPartition) {					
	        		if (map.containsKey(topicPartition.partition()) ){
	        			m_conn.m_consumer.seek(topicPartition, map.get(topicPartition.partition()));
	        		}
		        }
			}else if(param.getValue() instanceof Integer){
				Integer nOffset = (Integer)param.getValue();
				for (TopicPartition topicPartition : m_topicPartition) {
		        	m_conn.m_consumer.seek(topicPartition, nOffset);
		        }
			}
			m_ctx.setParamValue("offset_val", null);
		}
	}
	
	// �Դ������������ߵ�offset��������
	@SuppressWarnings("unchecked")
	private void setComsumerOffset(List<Integer> partitions){	
		Param param = m_ctx.getParam("offset_val");
		if (param!=null){
			if (param.getValue() instanceof Map){
				Map<Integer, Integer> map = (Map<Integer, Integer>)param.getValue();
				
				for (Map.Entry<Integer, Integer> entry: map.entrySet()) {
					//�ҳ����������Ľ���(kafka_offset, kafka_poll�ķ�������)
					if (partitions.contains(entry.getKey()) && entry.getValue()>-1){ 
						TopicPartition topicPartition = new TopicPartition(m_conn.getTopic(), entry.getKey());
	        			m_conn.m_consumer.seek(topicPartition, entry.getValue());
					}
		        }

			}else if(param.getValue() instanceof Integer){
				Integer nOffset = (Integer)param.getValue();
				for (Integer partSN : partitions) {
					TopicPartition topicPartition = new TopicPartition(m_conn.getTopic(), partSN);
        			m_conn.m_consumer.seek(topicPartition, nOffset);
		        }
			}
			m_ctx.setParamValue("offset_val", null);
		}
	}
}