package com.uws.orientation.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.uws.core.hibernate.dao.impl.BaseDaoImpl;
import com.uws.core.util.DataUtil;
import com.uws.domain.orientation.StudentGuardianModel;
import com.uws.orientation.dao.IStudentGuardianDao;

/**
 * 
* @ClassName: StudentGuardianDaoImpl 
* @Description: TODO(学生监护人信息的DaoImpl) 
* @author wangcl
* @date 2015-7-23 下午9:20:48 
*
 */
@Repository("studentGuardianDaoImpl")
public class StudentGuardianDaoImpl extends BaseDaoImpl implements IStudentGuardianDao {
	
	/**
	 * 
	* @Title: getByStudentIdAndSeqNum 
	* @Description: TODO(通过学生Id和顺序号查询出监护人信息) 
	* @param  studentId 学生Id
	* @return StudentGuardianModel
	* @author wangcl
	 */
	@Override
	public StudentGuardianModel getByStudentIdAndSeqNum(String studentId,String seqNum){
		
		String hql = "from StudentGuardianModel sgm where sgm.studentInfo.id = ? and sgm.seqNum=?";
		List<StudentGuardianModel> list = this.query(hql,  new String[]{studentId,seqNum});
		if(list !=null && list.size()>0){
			return list.get(0);
		}else{
			return null;
		}
		
	}
	
}
