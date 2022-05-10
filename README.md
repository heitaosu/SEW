# 注油系统API文档

标签（空格分隔）： api

---

文档在线地址 https://www.zybuluo.com/jiangnan308/note/2234888

接口域名:http://124.223.9.14:8080

例如登录接口使用POST请求：http://124.223.9.14:8080/crm/user/login.json?username=suchao&password=123456

**1. 接口定义**

**1.1 密码登录**

            1.1.1  接口说明：密码登录
            1.1.2  请求方式：POST
            1.1.3  接口地址 /crm/user/login.json
            1.1.4  参数说明:

| 参数名称 | 类型  | 是否允许为空 | 描述  |
| -------- | ----  | :---:          |:---: |
| username | string |  否         | 用户名 |
| password | string |  否         |密码   |


            1.1.5 接口返回值

``` json
        {
            "code": 200,
            "message": "SUCCESS",
            "data": {
                "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZWFsTmFtZSI6IuiLj-i2hSIsImxvZ2luTmFtZSI6InN1Y2hhbyIsImV4cCI6MTY0Nzc5ODI2MSwidXNlcklkIjoiOCJ9.O1a3HXWcIbZcSOXtys8tCuyvnlHE9Ia607hqFssgBxM",
                "user": {
                    "id": 8,
                    "username": "suchao",     #用户名（不能为空）
                    "password": null,         #密码（新增或修改密码时不能为空）
                    "realName": "苏超",       #用户真实姓名(不能为空)
                    "userLevel": null,        #用户等级 0:管理员 1:普通用户 （不能为空 默认 1）
                    "registerDate": 1647681249229,  #注册时间（可以为空）
                    "state": 1                      #状态 0:无效(未删除)  1:有效(未删除) （不能为空默认 1)
                }
            }
        }
```


        1.1.6 接口返说明
            code:200表示成功 其他表示失败
            data 接口返回的数据
                access_token：是该用户登录后，调用其他请求的一个身份信息，后面所有的请求都需要用到该值,使用方法为将该值放入到http请求头中去形如：access_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZWFsTmFtZSI6IuiLj-i2hSIsImxvZ2luTmFtZSI6InN1Y2hhbyIsImV4cCI6MTY0Nzc5ODI2MSwidXNlcklkIjoiOCJ9.O1a3HXWcIbZcSOXtys8tCuyvnlHE9Ia607hqFssgBxM





**1.2 新增或者修改用户**

        1.2.1  接口说明：新增或者修改用户(id为空则是新增，否则是修改，修改包含修改密码，删除用户设置state=0)
        1.2.2  请求方式：POST
        1.2.3  接口地址 /crm/user/save.json
        1.2.4  参数说明: 参考1.1.5 接口返回值







**1.3 查询用户列表**

        1.3.1  接口说明：查询用户列表
        1.3.2  请求方式：GET
        1.3.3  接口地址 /crm/user/list.json
        1.3.4  参数说明: page:当前是第几页(默认1)  size:每页返回多少条(默认10)
        1.3.5 接口返回值

``` json

{
    "code": 200,
    "message": "SUCCESS",
    "data": {
        "pageNum": 1,    #当前是第几页
        "pageSize": 3,   #总共有多少页
        "size": 3,       #当前页总共有多少条数据
        "orderBy": null,
        "startRow": 1,
        "endRow": 3,
        "total": 8,      #系统总共有多少条数据
        "pages": 3,
        "list": [        #返回的具体的数据
            {
                "id": 1,
                "username": "苏超",
                "password": "123456",
                "realName": null,
                "userLevel": null,
                "registerDate": 1647677050138,
                "state": 1
            },
            {
                "id": 2,
                "username": "苏超",
                "password": "123456",
                "realName": null,
                "userLevel": null,
                "registerDate": 1647677063528,
                "state": 1
            },
            {
                "id": 8,
                "username": "suchao",
                "password": "123456",
                "realName": "苏超",
                "userLevel": null,
                "registerDate": 1647681249229,
                "state": 1
            }
        ],
        "prePage": 0,
        "nextPage": 2,
        "isFirstPage": true,
        "isLastPage": false,
        "hasPreviousPage": false,
        "hasNextPage": true,
        "navigatePages": 8,
        "navigatepageNums": [
            1,
            2,
            3
        ],
        "navigateFirstPage": 1,
        "navigateLastPage": 3,
        "lastPage": 3,
        "firstPage": 1
    }
}
    
```



        1.13.6 接口返说明请参考接口返回值中带注释的字段，其他的字段可以忽略





**1.4 操作页面按钮**

        1.4.1  接口说明：该接口控制加油操作页面的所有按钮
        1.4.2  请求方式：POST
        1.4.3  接口地址 /crm/monitor/btn.json
        1.4.4  参数说明: type 对应关系请参考1.7接口说明（按钮的值）
                         value 1:开 0:关 默认 1 当按钮为 循环OR注油开关时要根据实际值传递 
        1.4.5 接口返回值

``` json
        {
            "code": 200,
            "message": "SUCCESS",
            "data": null
        }
```



**1.5 设置操作界面属性值**

        1.4.1  接口说明：该接口控制加油操作页面的所有可以手动修改的值
        1.4.2  请求方式：POST
        1.4.3  接口地址 /crm/monitor/setValue.json
        1.4.4  参数说明:  type 对应关系请参考1.7接口说明(页面显示/设定的值)
                          value 设置的值
        1.4.5 接口返回值

``` json
        {
            "code": 200,
            "message": "SUCCESS",
            "data": null
        }
```

**1.6 扫码完成后开始加油**

        1.4.1  接口说明：扫码完成后调用开始加油的接口
        1.4.2  请求方式：POST
        1.4.3  接口地址 /crm/monitor/scanstart.json
        1.4.4  参数说明:  code:扫码枪输入的编码(订单号)必填
        1.4.5 接口返回值

``` json
        {
            "code": 200,
            "message": "SUCCESS",
            "data": null
        }
```


**1.7 获取加油操作页面初始值**

        1.7.1  接口说明：获取加油操作页面初始值(打开加油页面时调用)
                目前需要初始化的值有 下面四个
                51:吸油压力设定值
                52:注油流量偏差值 >> b
                53:自动注油量设定值 >> 注油目标值
                54:注油流量系数  >> K
        1.7.2  请求方式：GET
        1.7.3  接口地址 /crm/monitor/initValue.json
        1.7.4  参数无
        1.7.5 接口返回值

``` json
       {
        "code": 200,
        "message": "SUCCESS",
        "data": [
            "val_53_301",
            "val_54_1.2",
            "val_52_0.2",
            "val_51_0.12"
        ]
    }
```

        1.7.6 返回值说明
        
        该接口返回的返回值形如
        
        [[form]_[type]_[msg],[form]_[type]_[msg],[form]_[type]_[msg]]
        
        参数的返回值可以参考接口1.8(监听服务端soket消息)的说明
        **区别是 本接口返回的是一个数组 而1.8返回的是单个值 其他一样**


**1.8 监听服务端soket消息**

        该功能通过wetsocket监听58080端口 信息实现，接受服务端发送过来的信息进行解析，然后按照解析规则，获取具体数据；
         
        消息内容
            [form]_[type]_[msg]
        解析规则
        from:来自哪里发来的消息，总共有四种 warn:告警消息 btn:按钮的值 val:页面显示/设定的值 alert:给用户弹窗提醒(需要页面alert弹窗提醒)
        type:类型对应关系如下
            val:页面显示/设定的值
                61:吸油压力当前值
                62:实时注油量
                63:剩余注油量
                51:吸油压力设定值
                52:注油流量偏差值 >> b
                53:自动注油量设定值 >> 注油目标值
                54:注油流量系数  >> K
                
            btn:按钮的值(1:开 0:关)
                11:手动加油启动按钮
                12:手动加油停止按钮
                13:扫码加油开始按钮
                14:扫码加油停止按钮
                15:报警复位按钮
                16:循环OR注油开关(1:循环 0:注油)
                17:消音按钮
                
            warn:告警消息 可以忽略对应关系直接显示msg即可
            alert:给用户弹窗提醒(需要页面alert弹窗提醒),可以忽略对应关系直接弹窗提示msg消息即可
                
        msg:具体的值
            如果来自告警消息或者页面显示的值，直接显示msg就可以
            如果是btn消息，1:开 0:关


注意事项：
1、监听操作界面的信息变化需要使用websocket，前端代码请参考websocket.html
2、扫描二维码的操作，需要前端监听扫码枪的输入信息，完成后请求接口；
    


    
    
            
            
           
            
            

    
 

