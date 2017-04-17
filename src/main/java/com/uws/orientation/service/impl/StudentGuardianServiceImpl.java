package com.uws.orientation.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.uws.core.base.BaseServiceImpl;
import com.uws.core.util.DataUtil;
import com.uws.domain.orientation.StudentGuardianModel;
import com.uws.orientation.dao.IStudentGuardianDao;
import com.uws.orientation.service.IStudentGuardianService;

/**
 * 
 * @ClassName: StudentGuardianServiceImpl
 * @Description: TODO(学生监护人信息的ServiceImpl)
 * @author wangcl
 * @date 2015-7-23 下午9:46:48
 * 
 */
@Repository("studentGuardianServiceImpl")
public class StudentGuardianServiceImpl extends BaseServiceImpl implements
		IStudentGuardianService {

	// 学生监护人信息Dao
	@Autowired
	private IStudentGuardianDao studentGuardianDao;

	/**
	 * 
	 * @Title: getByStudentIdAndSeqNum
	 * @Description: TODO(通过学生Id和顺序号查询出监护人信息)
	 * @param studentId
	 *            学生Id
	 * @return StudentGuardianModel
	 * @author wangcl
	 */
	@Override
	public StudentGuardianModel getByStudentIdAndSeqNum(String studentId,
			String seqNum) {

		return studentGuardianDao.getByStudentIdAndSeqNum(studentId, seqNum);
	}

	/**
	 * 
	 * @Title: getByStudentId
	 * @Description: TODO(通过学生Id查询出监护人信息)
	 * @param studentId
	 *            学生Id
	 * @return StudentGuardianModel
	 * @author wangcl
	 */
	@Override
	public StudentGuardianModel getByStudentId(String studentId) {

		StudentGuardianModel sgm = new StudentGuardianModel();

		// 父亲
		StudentGuardianModel sgmF = getByStudentIdAndSeqNum(studentId, "1");
		if (sgmF != null) {
			// 父亲Id
			sgm.setFatherId(sgmF.getId());
			// 父亲姓名
			sgm.setFatherName(sgmF.getGuardianName());
			// 父亲手机
			sgm.setFatherPhone(sgmF.getGuardianPhone());
			// 父亲住址
			sgm.setFatherAddress(sgmF.getGuardianAddress());
			// 父亲邮编
			sgm.setFatherPostCode(sgmF.getGuardianPostCode());
			// 父亲邮箱
			sgm.setFatherEmail(sgmF.getGuardianEmail());
			// 父亲工作单位
			sgm.setFatherWorkUnit(sgmF.getGuardianWorkUnit());
			//状态
			sgm.setStatus(sgmF.getStatus());
		}

		// 母亲
		StudentGuardianModel sgmM = getByStudentIdAndSeqNum(studentId, "2");
		if (sgmM != null) {
			// 母亲Id
			sgm.setMotherId(sgmM.getId());
			// 母亲姓名
			sgm.setMotherName(sgmM.getGuardianName());
			// 母亲手机
			sgm.setMotherPhone(sgmM.getGuardianPhone());
			// 母亲住址
			sgm.setMotherAddress(sgmM.getGuardianAddress());
			// 母亲邮编
			sgm.setMotherPostCode(sgmM.getGuardianPostCode());
			// 母亲邮箱
			sgm.setMotherEmail(sgmM.getGuardianEmail());
			// 母亲工作单位
			sgm.setMotherWorkUnit(sgmM.getGuardianWorkUnit());
			//状态
			sgm.setStatus(sgmM.getStatus());
		}
		
		// 监护人
		StudentGuardianModel sgmG = getByStudentIdAndSeqNum(studentId, "3");
		if (sgmG != null) {
			// 监护人Id
			sgm.setGuardianId(sgmG.getId());
			// 监护人姓名
			sgm.setGuardianName(sgmG.getGuardianName());
			// 监护人手机
			sgm.setGuardianPhone(sgmG.getGuardianPhone());
			// 监护人住址
			sgm.setGuardianAddress(sgmG.getGuardianAddress());
			// 监护人邮编
			sgm.setGuardianPostCode(sgmG.getGuardianPostCode());
			// 监护人邮箱
			sgm.setGuardianEmail(sgmG.getGuardianEmail());
			// 监护人工作单位
			sgm.setGuardianWorkUnit(sgmG.getGuardianWorkUnit());
			//状态
			sgm.setStatus(sgmG.getStatus());
		}

		return sgm;
	}

	/**
	 * 
	 * @Title: saveStudentGuardian
	 * @Description: TODO(保存学生监护人信息)
	 * @param studentGuardian
	 *            学生监护人Model
	 * @return void
	 * @author wangcl
	 */
	@Override
	public void saveStudentGuardian(StudentGuardianModel studentGuardian) {
		// TODO Auto-generated method stub
		// 生成UUID
		String id = DataUtil.createOID();
		// 设置Id
		studentGuardian.setId(id);
		//保存的状态
		studentGuardian.setStatus("0");
		
		//创建时间
		studentGuardian.setCreateTime(new Date());

		// 保存
		studentGuardianDao.save(studentGuardian);

	}

	/**
	 * 
	 * @Title: updateStudentGuardian
	 * @Description: TODO(修改学生监护人信息)
	 * @param studentGuardian
	 *            学生监护人Model
	 * @return void
	 * @author wangcl
	 */
	@Override
	public void updateStudentGuardian(StudentGuardianModel studentGuardian) {
		// 取得 学生监护人信息的Po
		StudentGuardianModel poTemp = (StudentGuardianModel) studentGuardianDao
				.get(StudentGuardianModel.class, studentGuardian.getId());
		/*
		 * //父亲姓名 poTemp.setFatherName(studentGuardian.getFatherName()); //父亲手机
		 * poTemp.setFatherPhone(studentGuardian.getFatherPhone()); //父亲住址
		 * poTemp.setFatherAddress(studentGuardian.getFatherAddress()); //父亲邮编
		 * poTemp.setFatherPostCode(studentGuardian.getFatherPostCode());
		 * //父亲Email poTemp.setFatherEmail(studentGuardian.getFatherEmail());
		 * //父亲工作单位
		 * poTemp.setFatherWorkUnit(studentGuardian.getFatherWorkUnit());
		 * 
		 * //母亲姓名 poTemp.setMotherName(studentGuardian.getMotherName()); //母亲手机
		 * poTemp.setMotherPhone(studentGuardian.getMotherPhone()); //母亲住址
		 * poTemp.setMotherAddress(studentGuardian.getMotherAddress()); //母亲邮编
		 * poTemp.setMotherPostCode(studentGuardian.getMotherPostCode());
		 * //母亲Email poTemp.setMotherEmail(studentGuardian.getMotherEmail());
		 * //母亲工作单位
		 * poTemp.setMotherWorkUnit(studentGuardian.getMotherWorkUnit());
		 */
		//新生信息
		poTemp.setStudentInfo(studentGuardian.getStudentInfo());
		// 监护人姓名
		poTemp.setGuardianName(studentGuardian.getGuardianName());
		// 监护人手机
		poTemp.setGuardianPhone(studentGuardian.getGuardianPhone());
		// 监护人住址
		poTemp.setGuardianAddress(studentGuardian.getGuardianAddress());
		// 监护人邮编
		poTemp.setGuardianPostCode(studentGuardian.getGuardianPostCode());
		// 监护人Email
		poTemp.setGuardianEmail(studentGuardian.getGuardianEmail());
		// 监护人工作单位
		poTemp.setGuardianWorkUnit(studentGuardian.getGuardianWorkUnit());
		// 顺序号
		poTemp.setSeqNum(studentGuardian.getSeqNum());

		// 状态
		//poTemp.setStatus(studentGuardian.getStatus());
		
		//修改时间
		poTemp.setUpdateTime(new Date());

		// 进行数据库更新
		studentGuardianDao.update(poTemp);
	}

}
