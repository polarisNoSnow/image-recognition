package com.polaris.image.mq;

import java.util.HashMap;
import java.util.Map;

import org.bytedeco.javacv.Frame;

/**
 * 自定义视频转换生产消费机制处理
 * @author polaris
 * @date 2019年11月26日
 */
public class HiperImageProcess {
	
	//消费队列
	private Map<Integer, Object> mq = new HashMap<Integer, Object>();
	
	/**
	 * 生产者
	 * @param key
	 * @param data
	 */
	public void consumer(Integer key,Object data) {
		mq.put(key, data);
	}
	
	/**
	 * 消费者
	 */
	public void producer() {
		Frame frame = null;
		for (int i = 0; i < mq.size(); i++) {
			for(;;) {
				if(mq.get(i) != null) {
					//TODO
					 break;
				}
			}
			
		}
	}
}
