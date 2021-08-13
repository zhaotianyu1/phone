/**@defgroup ����ͷ���
@{

@brief ����ͷ����ƶ�

@author duangang

@note  ������ͷ

@version 1.0.0 2019/7/15 
*/
#ifndef _TOS_MOTOR_H_
#define _TOS_MOTOR_H_

#ifdef __cplusplus
extern "C" {
#endif

/**
����ƶ�ģʽ
ͨ�������һ��������ʹ����ͷ֧��һ��ģʽ���ƶ�
**Ŀǰ����֧��ʹ����ͷ��ֱ��������**
**/
typedef enum {
	/* ��ֱ���� */
	EN_MOTOR_MOTION_VERTICAL_UP = 0,				 //��ֱ����
	EN_MOTOR_MOTION_VERTICAL_DOWN = 1, 			 //��ֱ����

	/* ˮƽ���� */
	EN_MOTOR_MOTION_HORIZONTAL_LEFT = 2,				 //ˮƽ����
	EN_MOTOR_MOTION_HORIZONTAL_RIGHT = 3,				//ˮƽ����

	/* ��ת���� */
	EN_MOTOR_MOTION_ROTATE_UP = 4, 			   //��ת����
	EN_MOTOR_MOTION_ROTATE_DOWN = 5,			   //��ת����

	/* ��ת���� */
	EN_MOTOR_MOTION_ROTATE_LEFT = 6,			  //��ת����
	EN_MOTOR_MOTION_ROTATE_RIGHT = 7,				//��ת����

	/*˳ʱ����ʱ����ת*/
	EN_MOTOR_MOTION_ROTATE_CIRCLEWISE = 8,			//˳ʱ����ת
	EN_MOTOR_MOTION_ROTATE_COUNTER_CIRCLEWISE = 9,	//��ʱ����ת
	
	EN_MOTION_MAX = 10				//�Ƿ��ж�
}EN_MOTOR_MOTION_T;

/**
����ͷ���״̬
**Ŀǰ�����д�ֱ����״̬**
**/
typedef enum {
	
	EN_MOTOR_STATUS_ROTATING= 0,				     //����ͷ�����ת��
	/* ��ֱ���� */
	EN_MOTOR_STATUS_VERTICAL_UP_MAXIMUM = 1, 			 //����ͷ��ֱ���ϵ�����
	EN_MOTOR_STATUS_VERTICAL_DOWN_MAXIMUM = 2, 	     //����ͷ��ֱ���µ��ײ�
	
	/* ˮƽ���� */
	EN_MOTOR_STATUS_HORIZONTAL_LEFT_MAXIMUM = 3,	     //����ͷˮƽ��������
	EN_MOTOR_STATUS_HORIZONTAL_RIGHT_MAXIMUM = 4,		 //����ͷˮƽ���ҵ�����

	/* ��ת���� */
	EN_MOTOR_STATUS_ROTATE_UP_MAXIMUM = 5, 			   //����ͷ��ת���ϵ�����
	EN_MOTOR_STATUS_ROTATE_DOWN_MAXIMUM = 6,			   //����ͷ��ת���µ��ײ�

	/* ��ת���� */
	EN_MOTOR_STATUS_ROTATE_LEFT_MAXIMUM = 7,			  //����ͷ��ת��������
	EN_MOTOR_STATUS_ROTATE_RIGHT_MAXIMUM = 8,				//����ͷ��ת���ҵ�����

	/*˳ʱ����ʱ��*/
	EN_MOTOR_STATUS_ROTATE_CIRCLEWISE_MAXIMUM = 9,			  //��Ļ˳ʱ����ת�����
	EN_MOTOR_STATUS_ROTATE_COUNTER_CIRCLEWISE_MAXIMUM = 10,				//��Ļ��ʱ����ת�����

	EN_MOTOR_STATUS_ROTATE_STUCK = 11,				//��ת��ס
	
	EN_MOTOR_STATUS_MAX = 12				//�Ƿ��ж�
}EN_MOTOR_STATUS_T;

typedef int32_t fpi_error;
typedef int8_t				    fpi_bool;
#define FPI_ERROR_SUCCESS								0x0000


/**
@brief ��������ĳ��ģʽת��ָ��ʱ��

@note �˽ӿ���ͬ��ʵ��

@param[in] motor_id ��ʶ�����ͨ�������ֻ��һ���������
@param[in] motion ����ͷ�ƶ�ģʽ���� EN_MOTOR_MOTION_T
@param[in] duration ת��ʱ�䣨��λms����Ĭ��ֵΪ0:��ʾת�������
@param[in] app_name ���ô˽ӿڵ�app name
@param[in] name_len app_name�ַ�������
@param[in] grab �Ƿ�����ռʽ����
@return �ɹ�����FPI_ERROR_SUCCESS;ʧ�ܷ�������ֵ
*/
fpi_error tos_motor_rotate(int32_t motor_id, EN_MOTOR_MOTION_T motion, int64_t duration, const char *app_name, int32_t name_len, fpi_bool grab);


/**
@brief ����ͷ�����︴λ����ʼ״̬

@note �˽ӿ���ͬ��ʵ��

@param[in] motor_id ��ʶ�����ͨ�������ֻ��һ���������
@return �ɹ�����FPI_ERROR_SUCCESS;ʧ�ܷ�������ֵ
*/
fpi_error  tos_motor_reset(int32_t motor_id);


/**
@brief ��ȡ����ͷ�����ﵱǰ״̬

@note �˽ӿ���ͬ��ʵ��

@param[in] motor_id ��ʶ�����ͨ�������ֻ��һ���������
@param[out] status ����ͷ���״̬����EN_MOTOR_STATUS_T
@return �ɹ�����FPI_ERROR_SUCCESS;ʧ�ܷ�������ֵ
*/

fpi_error tos_motor_get_status(int32_t motor_id, EN_MOTOR_STATUS_T *status);


/**
@brief ��ȡ��ת�����������תʱ��

@note �˽ӿ���ͬ��ʵ��

@param[in] motor_id ��ʶ�����ͨ�������ֻ��һ����ת���
@param[out] *rotate_time ��ת�������תʱ��,��λ:	ms
@return �ɹ�����FPI_ERROR_SUCCESS;ʧ�ܷ�������ֵ
*/

fpi_error tos_motor_get_rotate_time(int32_t motor_id, uint16_t *rotate_time);

#ifdef __cplusplus
}
#endif
#endif

