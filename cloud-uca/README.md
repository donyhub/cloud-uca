# 工程简介
认证服务器构建，其中包括授权码和密码两种模式。

## 授权码模式和密码模式的区别
### 密码模式
1. 在SecurityConfig中添加Bean:authenticationManager
2. 在AuthorizationServerConfig中注入：AuthenticationManager；
3. AuthorizationServerConfig中authorizationCodeServices不再需要；
4. endpoints设定authenticationManager
5. 修改clients中authorizedGrantTypes，添加password;

