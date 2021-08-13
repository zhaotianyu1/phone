
/**
@defgroup 系统相关函数
@

@brief 

@author 

@note 

@version 1.0.0 2017/3/22 
*/



#ifndef __TOS_SYSTEM_H__
#define __TOS_SYSTEM_H__


#ifndef ATTR_KEY_LENGTH_MAX
#define ATTR_KEY_LENGTH_MAX 64
#endif

#ifndef ATTR_VAL_TOTAL_SIZE_MAX
#define ATTR_VAL_TOTAL_SIZE_MAX 64*1024*1024
#endif


#ifdef __cplusplus
extern "C" {
#endif
typedef int32_t fpi_error;
/**
@brief 应用传入PID和TSA KEY，用以校验并注册。只有校验成功后，应用才能调用中间件的接口。
		该函数不会释放str_access_key的内存，str_access_key内存的管理由调用方自行处理。

@param[in] i_pid 应用的pid
@param[in] str_authorized_key 应用持有的TSA KEY
@param[in] i_len TSA KEY的长度，以字节为单位。最大长度为1038336（1014 * 1024）。
@return 成功返回FPI_ERROR_SUCCESS,失败返回FPI_ERROR_FAIL
*/
fpi_error tos_system_authorized(int32_t       i_pid, char* str_authorized_key, int32_t i_len);
#ifdef __cplusplus
}
#endif
/**  */

#endif





