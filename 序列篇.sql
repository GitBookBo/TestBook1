
note:

1.increment by n:����ֵÿ������n(����)

2.start with n: ��n��ʼ

3.{MAXVALUE n | NOMAXVALUE}�� �������ֵ

4.{MINVALUE n | NOMINVALUE}�� ������Сֵ��start with����С����Сֵ��

5.CYCLE | NOCYCLE          �� �Ƿ�ѭ�������鲻ʹ��

6.CACHE n | NOCACHE    �� �Ƿ����û��档

-- �������� 

--����һ������
-- Create sequence 
create sequence DEMO_1
minvalue 1
maxvalue 9999999999999999999999999999
start with 10
increment by 1
cache 20;


1.nextval : ������һ�����õ�����ֵ��

�����Ǳ���ͬ���û����ã�ÿ��Ҳ����һ��Ψһ��ֵ��

2.currval ����ȡ���е�ǰ��ֵ��

��currval����֮ǰ�����뱣֤nextval�Ѿ���ȡ��һ��ֵ��


--1.����в�������
select * from t2
insert into t2(type_id,fdept_name) values(DEMO_1.nextval,'��������');

--2.�鿴���еĵ�ǰֵ
select DEMO_1.currval from dual;

--3.��ȡ���е���һ��ֵ��
select DEMO_1.nextval from dual;


�޸�sequence��

ALTER SEQUENCE name

[INCREMENT BY n]      

[{MAXVALUE n | NOMAXVALUE}]

[{MINVALUE n | NOMINVALUE}]

[{CYCLE | NOCYCLE}]

[{CACHE n | NOCACHE}]

 

note��

1.���������е�ӵ���ߣ����߾���alterȨ��

2.�޸ĺ�����У�ֻ��֮���ֵ�����á�

3.�����޸�start with�������ģ�ֻ��ɾ�������´�����������

 

ɾ��sequence��

drop sequence seq_name;

���磺

drop sequence emp_id_seq;



