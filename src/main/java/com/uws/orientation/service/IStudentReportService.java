package com.uws.orientation.service;

import com.uws.core.base.IBaseService;
import com.uws.domain.orientation.StudentReportModel;

/**
 * 
* @ClassName: IStudentReportService 
* @Description: TODO(学生预报到信息的Service) 
* @author wangcl
* @date 2015-7-23 下午9:30:48 
*
 */
public interface IStudentReportService extends IBaseService {
	
	/**
	 * 
	* @Title: getByStudent 
	* @Description: TODO(通过学生Id查询出预报到信息) 
	* @param  studentId 学生Id
	* @return StudentReportModel
	* @author wangcl
	 */
	public StudentReportModel getByStudentId(String studentId);
	
	/**
	 * 
	* @Title: saveStudentReport 
	* @Description: TODO(保存学生预报到信息) 
	* @param  StudentReportModel 预报到Model
	* @return void
	* @author wangcl
	 */
	public void saveStudentReport( StudentReportModel studentReport );
	
	/**
	 * 
	* @Title: updateStudentReport 
	* @Description: TODO(修改学生预报到信息) 
	* @param  StudentReportModel 学生预报到Model
	* @return void
	* @author wangcl
	 */
	public void updateStudentReport( StudentReportModel studentReport );

}
