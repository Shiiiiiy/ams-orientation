package com.uws.orientation.service;

import com.uws.core.base.IBaseService;
import com.uws.domain.orientation.WelcomeSetModel;

/**
 * 
* @ClassName: IWelcomeSetService 
* @Description: TODO(迎新设置信息的Service) 
* @author wangcl
* @date 2015-7-24 下午2:46:48 
*
 */
public interface IWelcomeSetService extends IBaseService {
	
	/**
	 * 
	* @Title: getWelcomeSetById 
	* @Description: TODO(Id查询出迎新设置信息) 
	* @param  id id
	* @return WelcomeSetModel
	* @author wangcl
	 */
	public WelcomeSetModel getWelcomeSetById(String id);

	/**
	 * 
	* @Title: getWelcomeSet 
	* @Description: TODO(查询取出迎新设置信息) 
	* @return StudentGuardianModel
	* @author wangcl
	 */
	public WelcomeSetModel getWelcomeSet();
	
	/**
	 * 
	* @Title: saveWelcomeSet 
	* @Description: TODO(保存迎新设置信息) 
	* @param  welcomeSet 迎新设置信息Model
	* @return void
	* @author wangcl
	 */
	public void saveWelcomeSet( WelcomeSetModel welcomeSet );
	
	/**
	 * 
	* @Title: updateStudentGuardian 
	* @Description: TODO(修改学生监护人信息) 
	* @param  studentGuardian 学生监护人Model
	* @return void
	* @author wangcl
	 */
	public void updateWelcomeSet( WelcomeSetModel welcomeSet );
}
