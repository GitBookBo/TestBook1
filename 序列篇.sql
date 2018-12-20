
note:

1.increment by n:表明值每次增长n(步长)

2.start with n: 从n开始

3.{MAXVALUE n | NOMAXVALUE}： 设置最大值

4.{MINVALUE n | NOMINVALUE}： 设置最小值，start with不能小于最小值。

5.CYCLE | NOCYCLE          ： 是否循环，建议不使用

6.CACHE n | NOCACHE    ： 是否启用缓存。

-- 开发环境 

--新增一个序列
-- Create sequence 
create sequence DEMO_1
minvalue 1
maxvalue 9999999999999999999999999999
start with 10
increment by 1
cache 20;


1.nextval : 返回下一个可用的序列值。

就算是被不同的用户调用，每次也返回一个唯一的值。

2.currval ：获取序列当前的值。

在currval调用之前，必须保证nextval已经获取过一次值。


--1.向表中插入数据
select * from t2
insert into t2(type_id,fdept_name) values(DEMO_1.nextval,'我是序列');

--2.查看序列的当前值
select DEMO_1.currval from dual;

--3.获取序列的下一个值。
select DEMO_1.nextval from dual;


修改sequence：

ALTER SEQUENCE name

[INCREMENT BY n]      

[{MAXVALUE n | NOMAXVALUE}]

[{MINVALUE n | NOMINVALUE}]

[{CYCLE | NOCYCLE}]

[{CACHE n | NOCACHE}]

 

note：

1.必须是序列的拥有者，或者具有alter权限

2.修改后的序列，只对之后的值起作用。

3.不能修改start with，如果想改，只能删除，重新创建，启动。

 

删除sequence：

drop sequence seq_name;

例如：

drop sequence emp_id_seq;



