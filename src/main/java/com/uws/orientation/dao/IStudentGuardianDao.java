package com.uws.orientation.dao;

import com.uws.core.hibernate.dao.IBaseDao;
import com.uws.domain.orientation.StudentGuardianModel;

/**
 * 
* @ClassName: IStudentGuardianDao 
* @Description: TODO(学生监护人信息的Dao) 
* @author wangcl
* @date 2015-7-23 下午9:20:48 
*
 */
public interface IStudentGuardianDao extends IBaseDao {
	
	/**
	 * 
	* @Title: getByStudentIdAndSeqNum 
	* @Description: TODO(通过学生Id和顺序号查询出监护人信息) 
	* @param  studentId 学生Id
	* @return StudentGuardianModel
	* @author wangcl
	 */
	public StudentGuardianModel getByStudentIdAndSeqNum(String studentId,String seqNum);

}
