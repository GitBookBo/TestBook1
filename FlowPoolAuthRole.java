package com.amarsoft.app.lending.bizlets;

import com.amarsoft.are.sql.ASResultSet;
import com.amarsoft.are.sql.Transaction;
import com.amarsoft.are.util.DataConvert;
import com.amarsoft.biz.bizlet.Bizlet;

public class FlowPoolAuthRole extends Bizlet 
{
	@Override
	public Object  run(Transaction Sqlca) throws Exception{	
		String sCustomerID = (String)this.getAttribute("CustomerID");//客户ID
		String sPhaseNo = (String)this.getAttribute("PhaseNo");//阶段号
		String sObjectNo = (String)this.getAttribute("ObjectNo");//申请编号
		String sCustName="",sCertId="";
		String SSql="select ii.fullname,ii.certid from ind_info ii where ii.customerid='"+sCustomerID+"'";
		ASResultSet rs = Sqlca.getASResultSet(SSql);
		if(rs.next())
		{
			//查询当前客户的姓名、证件号码
			sCustName=DataConvert.toString(rs.getString("fullname"));
			sCertId=DataConvert.toString(rs.getString("certid"));
		}
		//获取处理人
		String sUserID = DataConvert.toString(Sqlca.getString("select userid from flow_task where serialno = (select max(FT.serialno)  from flow_task FT,"
				+"Business_apply BA,user_info  ui where ba.serialno = FT.ObjectNo and FT.ObjectType = 'CBCreditApply' "
				+"and BA.CustomerID = '"+sCustomerID+"'  and FT.PhaseNo = '"+sPhaseNo+"' and ft.userid=ui.userid 	and ui.status='1'  and ft.userid not in('OPS') )"));
		if("".equals(sUserID))return "0";
		//判断该用户是否有审批权限
		String sCount = DataConvert.toString(Sqlca.getString("select count(*) from user_role where roleid = 'DSPMR0100' and userid = '"+sUserID+"' "));
		if("0".equals(sCount))return "0";
		//判断该用户是否有审批权限
		//获取本笔业务的贷款品种
		String sBusinessType = DataConvert.toString(Sqlca.getString("select businesstype from business_apply where serialno = '"+sObjectNo+"'"));
		//本金余额
		double sBalanceSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(overduebalance + normalbalance),0) from loan_balance "
				                                                  +"where customerid = '"+sCustomerID+"' and COMMONFLAG is null"));
		//所有在途申请
		double sApproveSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(businesssum),0) from business_apply where customerid "
				                                                  +"= '"+sCustomerID+"' and approveresult is null"));
		//所有审批通过但未放款的
		double sPutoutWaitSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(businesssum),0) from business_putout where customerid = '"+sCustomerID+"' and putoutstatus = '0'"));
		
		//tangbo
		String bpCustomerid="";
		//查询所有已放款成功的数据的客户编号
		String SQL="select bp.customerid from ind_info ii,business_putout bp where ii.customerid=bp.customerid and bp.putoutstatus='1' and ii.spousename ='"+sCustName+"' and ii.spouseid ='"+sCertId+"'";
		rs = Sqlca.getASResultSet(SQL);
		if(rs.next())
		{
			bpCustomerid=DataConvert.toString(rs.getString("customerid"));
			System.out.println("夫妻共同申请需校验审批权限");
			//查询客户的本金余额(上一笔)
			double sSpouseSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(overduebalance + normalbalance),0) from loan_balance "
                    +"where customerid = '"+bpCustomerid+"' and COMMONFLAG is null"));
			double dBSum = sApproveSum + sSpouseSum;
			System.out.println("1初始化审批任务池申请号:"+sObjectNo +" 客户号："+sCustomerID +" 本金余额:"+sBalanceSum +" 在途申请金额:"+sApproveSum+" 审批通过待放款:"+sPutoutWaitSum);
			System.out.println("已放款成功配偶的本金余额："+sSpouseSum);
			String sSql = "select distinct am.AuthNo  from auth_model am, auth_catalog ac,auth_user  au "+
					" where am.authno = ac.authno and au.authno = ac.authno and au.status = '1' and au.userid = '"+sUserID+"' "+
					  " and ac.roleid = 'DSPMR0100'"+
					  " and am.businesstype like '%@"+sBusinessType+"@%'"+
					  " and am.L1SumMin <= "+dBSum+""+//--单笔下限【取任务】
					  " and am.L1SumMax >= "+dBSum+"";//--单笔上限【取任务】
			System.out.println("sSql---"+sSql);
			if(rs.next())
			{
				return "1";
			}
			rs.getStatement().close();
			return "0";
		}
		//查询所有审批通过未放款的数据的客户编号
		String SQL2="select bp.customerid from ind_info ii,business_putout bp where ii.customerid=bp.customerid and bp.putoutstatus='0' and ii.spousename ='"+sCustName+"' and ii.spouseid ='"+sCertId+"'";
		rs = Sqlca.getASResultSet(SQL2);
		if(rs.next())
		{
			bpCustomerid=DataConvert.toString(rs.getString("customerid"));
			System.out.println("夫妻共同申请需校验审批权限");
			//查询客户最终可贷金额(上一笔)
			double sSpouseSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(businesssum),0) from business_putout where customerid = '"+bpCustomerid+"' and putoutstatus = '0'"));
			double dBSum = sApproveSum + sSpouseSum;
			System.out.println("1初始化审批任务池申请号:"+sObjectNo +" 客户号："+sCustomerID +" 本金余额:"+sBalanceSum +" 在途申请金额:"+sApproveSum+" 审批通过待放款:"+sPutoutWaitSum);
			System.out.println("审批通过未放款配偶的最终可贷金额："+sSpouseSum);
			String sSql = "select distinct am.AuthNo  from auth_model am, auth_catalog ac,auth_user  au "+
					" where am.authno = ac.authno and au.authno = ac.authno and au.status = '1' and au.userid = '"+sUserID+"' "+
					  " and ac.roleid = 'DSPMR0100'"+
					  " and am.businesstype like '%@"+sBusinessType+"@%'"+
					  " and am.L1SumMin <= "+dBSum+""+//--单笔下限【取任务】
					  " and am.L1SumMax >= "+dBSum+"";//--单笔上限【取任务】
			System.out.println("sSql---"+sSql);
			if(rs.next())
			{
				return "1";
			}
			rs.getStatement().close();
			return "0";
		}
		//查询所有审批未通过阶段的客户编号
		String IIsID=DataConvert.toString(Sqlca.getString("select ii.customerid from ind_info ii where ii.spousename ='"+sCustName+"' and ii.spouseid ='"+sCertId+"'"));
		if(!"".equals(IIsID)){
			bpCustomerid=DataConvert.toString(Sqlca.getString("select bp.customerid from business_putout bp where bp.customerid='"+IIsID+"'"));
		}
		if("".equals(bpCustomerid))
		{
			//bpCustomerid=DataConvert.toString(rs.getString("customerid"));
			System.out.println("夫妻共同申请需校验审批权限");
			//查询客户申请金额(上一笔)
			double sSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(businesssum),0) from business_apply where customerid "
                    +"= '"+IIsID+"' and approveresult is null"));
			double dBSum = sApproveSum + sSum;
			System.out.println("1初始化审批任务池申请号:"+sObjectNo +" 客户号："+sCustomerID +" 在途申请金额:"+sApproveSum);
			System.out.println("审批未通过的申请金额："+sSum);
			String sSql = "select distinct am.AuthNo  from auth_model am, auth_catalog ac,auth_user  au "+
					" where am.authno = ac.authno and au.authno = ac.authno and au.status = '1' and au.userid = '"+sUserID+"' "+
					  " and ac.roleid = 'DSPMR0100'"+
					  " and am.businesstype like '%@"+sBusinessType+"@%'"+
					  " and am.L1SumMin <= "+dBSum+""+//--单笔下限【取任务】
					  " and am.L1SumMax >= "+dBSum+"";//--单笔上限【取任务】
			System.out.println("sSql---"+sSql);
			if(rs.next())
			{
				return "1";
			}
			rs.getStatement().close();
			return "0";
		}
		
		double dBusinessSum = sBalanceSum + sApproveSum + sPutoutWaitSum;
		System.out.println("1初始化审批任务池申请号:"+sObjectNo +" 客户号："+sCustomerID +" 本金余额:"+sBalanceSum +" 在途申请金额:"+sApproveSum+" 审批通过待放款:"+sPutoutWaitSum);
		
		//2、--------------------判断此审批人是否有审批权限
		String sSql = "select distinct am.AuthNo  from auth_model am, auth_catalog ac,auth_user  au "+
			" where am.authno = ac.authno and au.authno = ac.authno and au.status = '1' and au.userid = '"+sUserID+"' "+
			  " and ac.roleid = 'DSPMR0100'"+
			  " and am.businesstype like '%@"+sBusinessType+"@%'"+
			  " and am.L1SumMin <= "+dBusinessSum+""+//--单笔下限【取任务】
			  " and am.L1SumMax >= "+dBusinessSum+"";//--单笔上限【取任务】
		System.out.println("sSql---"+sSql);
		//ASResultSet rs = Sqlca.getASResultSet(sSql);
		if(rs.next())
		{
			return "1";
		}
		rs.getStatement().close();
		return "0";
	}
	
}
