package com.uws.orientation.dao.impl;

import org.springframework.stereotype.Repository;

import com.uws.core.hibernate.dao.impl.BaseDaoImpl;
import com.uws.core.util.DataUtil;
import com.uws.domain.orientation.StudentReportModel;
import com.uws.orientation.dao.IStudentReportDao;

/**
 * 
* @ClassName: StudentReportDaoImpl 
* @Description: TODO(学生预报到信息的DaoImpl) 
* @author wangcl
* @date 2015-7-24 下午2:40:48 
*
 */
@Repository("studentReportDaoImpl")
public class StudentReportDaoImpl extends BaseDaoImpl implements IStudentReportDao {
	
	/**
	 * 
	* @Title: getByStudent 
	* @Description: TODO(通过学生Id查询出监护人信息) 
	* @param  studentId 学生Id
	* @return StudentReportModel
	* @author wangcl
	 */
	@Override
	public StudentReportModel getByStudentId(String studentId){
		
		String hql = "from StudentReportModel srm where srm.studentInfo.id = ?";
		
		Object object = this.queryUnique(hql, studentId);
		
		return DataUtil.isNotNull(object) ? (StudentReportModel) object : null;
	}
	
}
