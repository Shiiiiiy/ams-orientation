package com.uws.orientation.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.uws.core.base.BaseServiceImpl;
import com.uws.domain.orientation.WelcomeSetModel;
import com.uws.orientation.dao.IWelcomeSetDao;
import com.uws.orientation.service.IWelcomeSetService;

/**
 * 
 * @ClassName: IWelcomeSetService
 * @Description: TODO(迎新设置信息的Service)
 * @author wangcl
 * @date 2015-7-24 下午2:46:48
 * 
 */
@Repository("welcomeSetServiceImpl")
public class WelcomeSetServiceImpl extends BaseServiceImpl implements
		IWelcomeSetService {

	// 迎新设置信息Dao
	@Autowired
	private IWelcomeSetDao welcomeSetDao;

	/**
	 * 
	 * @Title: getWelcomeSetById
	 * @Description: TODO(Id查询出迎新设置信息)
	 * @param id
	 *            id
	 * @return WelcomeSetModel
	 * @author wangcl
	 */
	@Override
	public WelcomeSetModel getWelcomeSetById(String id) {

		return welcomeSetDao.getById(id);
	}

	/**
	 * 
	 * @Title: getWelcomeSet
	 * @Description: TODO(查询取出迎新设置信息)
	 * @return StudentGuardianModel
	 * @author wangcl
	 */
	@Override
	public WelcomeSetModel getWelcomeSet() {

		return welcomeSetDao.getWelcomeSet();

	}

	/**
	 * 
	 * @Title: saveWelcomeSet
	 * @Description: TODO(保存迎新设置信息)
	 * @param welcomeSet
	 *            迎新设置信息Model
	 * @return void
	 * @author wangcl
	 */
	@Override
	public void saveWelcomeSet(WelcomeSetModel welcomeSet) {
		// TODO Auto-generated method stub
		// 生成UUID
		//String id = DataUtil.createOID();
		// 设置Id
		//welcomeSet.setId(id);

		// 保存
		welcomeSetDao.save(welcomeSet);

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
	public void updateWelcomeSet(WelcomeSetModel welcomeSet) {
		// 取得迎新设置信息
		WelcomeSetModel poTemp = welcomeSetDao.getById(welcomeSet.getId());
		
		//spring的对象的属性copy
		BeanUtils.copyProperties(welcomeSet, poTemp, new String[] {"id","status", "createTime" });
		
		// 进行数据库更新
		welcomeSetDao.update(poTemp);

	}

}
