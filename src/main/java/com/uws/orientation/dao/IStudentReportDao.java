package com.uws.orientation.dao;

import com.uws.core.hibernate.dao.IBaseDao;
import com.uws.domain.orientation.StudentReportModel;

/**
 * 
* @ClassName: IStudentReportDao 
* @Description: TODO(学生预报到信息的Dao) 
* @author wangcl
* @date 2015-7-24 下午2:40:48 
*
 */
public interface IStudentReportDao extends IBaseDao {
			
	/**
	* 
	* @Title: getByStudent 
	* @Description: TODO(通过学生Id查询出监护人信息) 
	* @param  studentId 学生Id
	* @return StudentGuardianModel
	* @author wangcl
	*/
	public StudentReportModel getByStudentId(String studentId);

}
