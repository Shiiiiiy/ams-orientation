package com.uws.orientation.service;

import com.uws.core.base.IBaseService;
import com.uws.domain.orientation.StudentGuardianModel;

/**
 * 
* @ClassName: IStudentGuardianService 
* @Description: TODO(学生监护人基本信息的Service) 
* @author wangcl
* @date 2015-7-23 下午9:30:48 
*
 */
public interface IStudentGuardianService extends IBaseService {
	
	/**
	 * 
	* @Title: getByStudentIdAndSeqNum 
	* @Description: TODO(通过学生Id和顺序号查询出监护人信息) 
	* @param  studentId 学生Id
	* @return StudentGuardianModel
	* @author wangcl
	 */
	public StudentGuardianModel getByStudentIdAndSeqNum(String studentId,String seqNum);
	
	/**
	 * 
	 * @Title: getByStudentId
	 * @Description: TODO(通过学生Id查询出监护人信息)
	 * @param studentId
	 *            学生Id
	 * @return StudentGuardianModel
	 * @author wangcl
	 */
	public StudentGuardianModel getByStudentId(String studentId);
	
	/**
	 * 
	* @Title: saveStudentGuardian 
	* @Description: TODO(保存学生监护人信息) 
	* @param  studentGuardian 学生监护人Model
	* @return void
	* @author wangcl
	 */
	public void saveStudentGuardian( StudentGuardianModel studentGuardian );
	
	/**
	 * 
	* @Title: updateStudentGuardian 
	* @Description: TODO(修改学生监护人信息) 
	* @param  studentGuardian 学生监护人Model
	* @return void
	* @author wangcl
	 */
	public void updateStudentGuardian( StudentGuardianModel studentGuardian );

}
