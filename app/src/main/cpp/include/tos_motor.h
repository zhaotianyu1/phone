/**@defgroup 摄像头马达
@{

@brief 摄像头马达移动

@author duangang

@note  有摄像头

@version 1.0.0 2019/7/15 
*/
#ifndef _TOS_MOTOR_H_
#define _TOS_MOTOR_H_

#ifdef __cplusplus
extern "C" {
#endif

/**
马达移动模式
通常情况下一个马达仅能使摄像头支持一种模式的移动
**目前马达仅支持使摄像头垂直向上向下**
**/
typedef enum {
	/* 垂直上下 */
	EN_MOTOR_MOTION_VERTICAL_UP = 0,				 //垂直向上
	EN_MOTOR_MOTION_VERTICAL_DOWN = 1, 			 //垂直向下

	/* 水平左右 */
	EN_MOTOR_MOTION_HORIZONTAL_LEFT = 2,				 //水平向左
	EN_MOTOR_MOTION_HORIZONTAL_RIGHT = 3,				//水平向右

	/* 旋转上下 */
	EN_MOTOR_MOTION_ROTATE_UP = 4, 			   //旋转向上
	EN_MOTOR_MOTION_ROTATE_DOWN = 5,			   //旋转向下

	/* 旋转左右 */
	EN_MOTOR_MOTION_ROTATE_LEFT = 6,			  //旋转向左
	EN_MOTOR_MOTION_ROTATE_RIGHT = 7,				//旋转向右

	/*顺时针逆时针旋转*/
	EN_MOTOR_MOTION_ROTATE_CIRCLEWISE = 8,			//顺时针旋转
	EN_MOTOR_MOTION_ROTATE_COUNTER_CIRCLEWISE = 9,	//逆时针旋转
	
	EN_MOTION_MAX = 10				//非法判断
}EN_MOTOR_MOTION_T;

/**
摄像头马达状态
**目前马达仅有垂直上下状态**
**/
typedef enum {
	
	EN_MOTOR_STATUS_ROTATING= 0,				     //摄像头马达旋转中
	/* 垂直上下 */
	EN_MOTOR_STATUS_VERTICAL_UP_MAXIMUM = 1, 			 //摄像头垂直向上到顶部
	EN_MOTOR_STATUS_VERTICAL_DOWN_MAXIMUM = 2, 	     //摄像头垂直向下到底部
	
	/* 水平左右 */
	EN_MOTOR_STATUS_HORIZONTAL_LEFT_MAXIMUM = 3,	     //摄像头水平向左到最左
	EN_MOTOR_STATUS_HORIZONTAL_RIGHT_MAXIMUM = 4,		 //摄像头水平向右到最右

	/* 旋转上下 */
	EN_MOTOR_STATUS_ROTATE_UP_MAXIMUM = 5, 			   //摄像头旋转向上到顶部
	EN_MOTOR_STATUS_ROTATE_DOWN_MAXIMUM = 6,			   //摄像头旋转向下到底部

	/* 旋转左右 */
	EN_MOTOR_STATUS_ROTATE_LEFT_MAXIMUM = 7,			  //摄像头旋转向左到最左
	EN_MOTOR_STATUS_ROTATE_RIGHT_MAXIMUM = 8,				//摄像头旋转向右到最右

	/*顺时针逆时针*/
	EN_MOTOR_STATUS_ROTATE_CIRCLEWISE_MAXIMUM = 9,			  //屏幕顺时针旋转到最大
	EN_MOTOR_STATUS_ROTATE_COUNTER_CIRCLEWISE_MAXIMUM = 10,				//屏幕逆时针旋转到最大

	EN_MOTOR_STATUS_ROTATE_STUCK = 11,				//旋转卡住
	
	EN_MOTOR_STATUS_MAX = 12				//非法判断
}EN_MOTOR_STATUS_T;

typedef int32_t fpi_error;
typedef int8_t				    fpi_bool;
#define FPI_ERROR_SUCCESS								0x0000


/**
@brief 电机马达以某个模式转动指定时间

@note 此接口是同步实现

@param[in] motor_id 标识电机马达，通常情况下只有一个升降马达
@param[in] motion 摄像头移动模式，见 EN_MOTOR_MOTION_T
@param[in] duration 转动时间（单位ms），默认值为0:表示转动到最大
@param[in] app_name 调用此接口的app name
@param[in] name_len app_name字符串长度
@param[in] grab 是否是抢占式调用
@return 成功返回FPI_ERROR_SUCCESS;失败返回其他值
*/
fpi_error tos_motor_rotate(int32_t motor_id, EN_MOTOR_MOTION_T motion, int64_t duration, const char *app_name, int32_t name_len, fpi_bool grab);


/**
@brief 摄像头电机马达复位到初始状态

@note 此接口是同步实现

@param[in] motor_id 标识电机马达，通常情况下只有一个升降马达
@return 成功返回FPI_ERROR_SUCCESS;失败返回其他值
*/
fpi_error  tos_motor_reset(int32_t motor_id);


/**
@brief 获取摄像头电机马达当前状态

@note 此接口是同步实现

@param[in] motor_id 标识电机马达，通常情况下只有一个升降马达
@param[out] status 摄像头电机状态，见EN_MOTOR_STATUS_T
@return 成功返回FPI_ERROR_SUCCESS;失败返回其他值
*/

fpi_error tos_motor_get_status(int32_t motor_id, EN_MOTOR_STATUS_T *status);


/**
@brief 获取旋转屏电机马达的旋转时间

@note 此接口是同步实现

@param[in] motor_id 标识电机马达，通常情况下只有一个旋转马达
@param[out] *rotate_time 旋转电机的旋转时间,单位:	ms
@return 成功返回FPI_ERROR_SUCCESS;失败返回其他值
*/

fpi_error tos_motor_get_rotate_time(int32_t motor_id, uint16_t *rotate_time);

#ifdef __cplusplus
}
#endif
#endif

