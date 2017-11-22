package com.amarsoft.app.lending.bizlets;

import com.amarsoft.are.sql.ASResultSet;
import com.amarsoft.are.sql.Transaction;
import com.amarsoft.are.util.DataConvert;
import com.amarsoft.biz.bizlet.Bizlet;

public class FlowPoolAuthRole extends Bizlet 
{
	@Override
	public Object  run(Transaction Sqlca) throws Exception{	
		String sCustomerID = (String)this.getAttribute("CustomerID");//�ͻ�ID
		String sPhaseNo = (String)this.getAttribute("PhaseNo");//�׶κ�
		String sObjectNo = (String)this.getAttribute("ObjectNo");//������
		String sCustName="",sCertId="";
		String SSql="select ii.fullname,ii.certid from ind_info ii where ii.customerid='"+sCustomerID+"'";
		ASResultSet rs = Sqlca.getASResultSet(SSql);
		if(rs.next())
		{
			//��ѯ��ǰ�ͻ���������֤������
			sCustName=DataConvert.toString(rs.getString("fullname"));
			sCertId=DataConvert.toString(rs.getString("certid"));
		}
		//��ȡ������
		String sUserID = DataConvert.toString(Sqlca.getString("select userid from flow_task where serialno = (select max(FT.serialno)  from flow_task FT,"
				+"Business_apply BA,user_info  ui where ba.serialno = FT.ObjectNo and FT.ObjectType = 'CBCreditApply' "
				+"and BA.CustomerID = '"+sCustomerID+"'  and FT.PhaseNo = '"+sPhaseNo+"' and ft.userid=ui.userid 	and ui.status='1'  and ft.userid not in('OPS') )"));
		if("".equals(sUserID))return "0";
		//�жϸ��û��Ƿ�������Ȩ��
		String sCount = DataConvert.toString(Sqlca.getString("select count(*) from user_role where roleid = 'DSPMR0100' and userid = '"+sUserID+"' "));
		if("0".equals(sCount))return "0";
		//�жϸ��û��Ƿ�������Ȩ��
		//��ȡ����ҵ��Ĵ���Ʒ��
		String sBusinessType = DataConvert.toString(Sqlca.getString("select businesstype from business_apply where serialno = '"+sObjectNo+"'"));
		//�������
		double sBalanceSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(overduebalance + normalbalance),0) from loan_balance "
				                                                  +"where customerid = '"+sCustomerID+"' and COMMONFLAG is null"));
		//������;����
		double sApproveSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(businesssum),0) from business_apply where customerid "
				                                                  +"= '"+sCustomerID+"' and approveresult is null"));
		//��������ͨ����δ�ſ��
		double sPutoutWaitSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(businesssum),0) from business_putout where customerid = '"+sCustomerID+"' and putoutstatus = '0'"));
		
		//tangbo
		String bpCustomerid="";
		//��ѯ�����ѷſ�ɹ������ݵĿͻ����
		String SQL="select bp.customerid from ind_info ii,business_putout bp where ii.customerid=bp.customerid and bp.putoutstatus='1' and ii.spousename ='"+sCustName+"' and ii.spouseid ='"+sCertId+"'";
		rs = Sqlca.getASResultSet(SQL);
		if(rs.next())
		{
			bpCustomerid=DataConvert.toString(rs.getString("customerid"));
			System.out.println("���޹�ͬ������У������Ȩ��");
			//��ѯ�ͻ��ı������(��һ��)
			double sSpouseSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(overduebalance + normalbalance),0) from loan_balance "
                    +"where customerid = '"+bpCustomerid+"' and COMMONFLAG is null"));
			double dBSum = sApproveSum + sSpouseSum;
			System.out.println("1��ʼ����������������:"+sObjectNo +" �ͻ��ţ�"+sCustomerID +" �������:"+sBalanceSum +" ��;������:"+sApproveSum+" ����ͨ�����ſ�:"+sPutoutWaitSum);
			System.out.println("�ѷſ�ɹ���ż�ı�����"+sSpouseSum);
			String sSql = "select distinct am.AuthNo  from auth_model am, auth_catalog ac,auth_user  au "+
					" where am.authno = ac.authno and au.authno = ac.authno and au.status = '1' and au.userid = '"+sUserID+"' "+
					  " and ac.roleid = 'DSPMR0100'"+
					  " and am.businesstype like '%@"+sBusinessType+"@%'"+
					  " and am.L1SumMin <= "+dBSum+""+//--�������ޡ�ȡ����
					  " and am.L1SumMax >= "+dBSum+"";//--�������ޡ�ȡ����
			System.out.println("sSql---"+sSql);
			if(rs.next())
			{
				return "1";
			}
			rs.getStatement().close();
			return "0";
		}
		//��ѯ��������ͨ��δ�ſ�����ݵĿͻ����
		String SQL2="select bp.customerid from ind_info ii,business_putout bp where ii.customerid=bp.customerid and bp.putoutstatus='0' and ii.spousename ='"+sCustName+"' and ii.spouseid ='"+sCertId+"'";
		rs = Sqlca.getASResultSet(SQL2);
		if(rs.next())
		{
			bpCustomerid=DataConvert.toString(rs.getString("customerid"));
			System.out.println("���޹�ͬ������У������Ȩ��");
			//��ѯ�ͻ����տɴ����(��һ��)
			double sSpouseSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(businesssum),0) from business_putout where customerid = '"+bpCustomerid+"' and putoutstatus = '0'"));
			double dBSum = sApproveSum + sSpouseSum;
			System.out.println("1��ʼ����������������:"+sObjectNo +" �ͻ��ţ�"+sCustomerID +" �������:"+sBalanceSum +" ��;������:"+sApproveSum+" ����ͨ�����ſ�:"+sPutoutWaitSum);
			System.out.println("����ͨ��δ�ſ���ż�����տɴ���"+sSpouseSum);
			String sSql = "select distinct am.AuthNo  from auth_model am, auth_catalog ac,auth_user  au "+
					" where am.authno = ac.authno and au.authno = ac.authno and au.status = '1' and au.userid = '"+sUserID+"' "+
					  " and ac.roleid = 'DSPMR0100'"+
					  " and am.businesstype like '%@"+sBusinessType+"@%'"+
					  " and am.L1SumMin <= "+dBSum+""+//--�������ޡ�ȡ����
					  " and am.L1SumMax >= "+dBSum+"";//--�������ޡ�ȡ����
			System.out.println("sSql---"+sSql);
			if(rs.next())
			{
				return "1";
			}
			rs.getStatement().close();
			return "0";
		}
		//��ѯ��������δͨ���׶εĿͻ����
		String IIsID=DataConvert.toString(Sqlca.getString("select ii.customerid from ind_info ii where ii.spousename ='"+sCustName+"' and ii.spouseid ='"+sCertId+"'"));
		if(!"".equals(IIsID)){
			bpCustomerid=DataConvert.toString(Sqlca.getString("select bp.customerid from business_putout bp where bp.customerid='"+IIsID+"'"));
		}
		if("".equals(bpCustomerid))
		{
			//bpCustomerid=DataConvert.toString(rs.getString("customerid"));
			System.out.println("���޹�ͬ������У������Ȩ��");
			//��ѯ�ͻ�������(��һ��)
			double sSum = DataConvert.toDouble( Sqlca.getString("select nvl(sum(businesssum),0) from business_apply where customerid "
                    +"= '"+IIsID+"' and approveresult is null"));
			double dBSum = sApproveSum + sSum;
			System.out.println("1��ʼ����������������:"+sObjectNo +" �ͻ��ţ�"+sCustomerID +" ��;������:"+sApproveSum);
			System.out.println("����δͨ���������"+sSum);
			String sSql = "select distinct am.AuthNo  from auth_model am, auth_catalog ac,auth_user  au "+
					" where am.authno = ac.authno and au.authno = ac.authno and au.status = '1' and au.userid = '"+sUserID+"' "+
					  " and ac.roleid = 'DSPMR0100'"+
					  " and am.businesstype like '%@"+sBusinessType+"@%'"+
					  " and am.L1SumMin <= "+dBSum+""+//--�������ޡ�ȡ����
					  " and am.L1SumMax >= "+dBSum+"";//--�������ޡ�ȡ����
			System.out.println("sSql---"+sSql);
			if(rs.next())
			{
				return "1";
			}
			rs.getStatement().close();
			return "0";
		}
		
		double dBusinessSum = sBalanceSum + sApproveSum + sPutoutWaitSum;
		System.out.println("1��ʼ����������������:"+sObjectNo +" �ͻ��ţ�"+sCustomerID +" �������:"+sBalanceSum +" ��;������:"+sApproveSum+" ����ͨ�����ſ�:"+sPutoutWaitSum);
		
		//2��--------------------�жϴ��������Ƿ�������Ȩ��
		String sSql = "select distinct am.AuthNo  from auth_model am, auth_catalog ac,auth_user  au "+
			" where am.authno = ac.authno and au.authno = ac.authno and au.status = '1' and au.userid = '"+sUserID+"' "+
			  " and ac.roleid = 'DSPMR0100'"+
			  " and am.businesstype like '%@"+sBusinessType+"@%'"+
			  " and am.L1SumMin <= "+dBusinessSum+""+//--�������ޡ�ȡ����
			  " and am.L1SumMax >= "+dBusinessSum+"";//--�������ޡ�ȡ����
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
