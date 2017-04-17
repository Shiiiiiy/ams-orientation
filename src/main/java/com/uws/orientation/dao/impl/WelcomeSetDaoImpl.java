package com.uws.orientation.dao.impl;

import org.springframework.stereotype.Repository;

import com.uws.core.hibernate.dao.impl.BaseDaoImpl;
import com.uws.core.util.DataUtil;
import com.uws.domain.orientation.WelcomeSetModel;
import com.uws.orientation.dao.IWelcomeSetDao;

/**
 * 
* @ClassName: IWelcomeSetDao 
* @Description: TODO(迎新设置信息的Dao) 
* @author wangcl
* @date 2015-7-24 下午2:20:48 
*
 */
@Repository("welcomeSetDaoImpl")
public class WelcomeSetDaoImpl extends BaseDaoImpl implements IWelcomeSetDao {
	
	/**
	 * 
	* @Title: getgetByIdByStudent 
	* @Description: TODO(通过Id查询出迎新设置信息) 
	* @param  id 迎新设置Id
	* @return WelcomeSetModel
	* @author wangcl
	 */
	public WelcomeSetModel getById(String id){
		return (WelcomeSetModel) this.get(
				WelcomeSetModel.class, id);
		
	}
	
	/**
	 * 
	* @Title: getWelcomeSet 
	* @Description: TODO(查询出迎新设置信息) 
	* @return WelcomeSetModel
	* @author wangcl
	 */
	public WelcomeSetModel getWelcomeSet(){
		
		String hql = "from WelcomeSetModel ";
		
		Object object = this.queryUnique(hql);
		
		return DataUtil.isNotNull(object) ? (WelcomeSetModel) object : null;
		
	}


}
