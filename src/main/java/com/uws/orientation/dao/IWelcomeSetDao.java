package com.uws.orientation.dao;

import com.uws.core.hibernate.dao.IBaseDao;
import com.uws.domain.orientation.WelcomeSetModel;

/**
 * 
* @ClassName: IWelcomeSetDao 
* @Description: TODO(迎新设置信息的Dao) 
* @author wangcl
* @date 2015-7-24 下午2:20:48 
*
 */
public interface  IWelcomeSetDao extends IBaseDao {
	
	/**
	 * 
	* @Title: getgetByIdByStudent 
	* @Description: TODO(通过Id查询出迎新设置信息) 
	* @param  id 迎新设置Id
	* @return WelcomeSetModel
	* @author wangcl
	 */
	public WelcomeSetModel getById(String id);
	
	/**
	 * 
	* @Title: getWelcomeSet 
	* @Description: TODO(查询出迎新设置信息) 
	* @return WelcomeSetModel
	* @author wangcl
	 */
	public WelcomeSetModel getWelcomeSet();
}
