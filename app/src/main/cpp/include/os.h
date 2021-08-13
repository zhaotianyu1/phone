#ifndef _OS_H_
#define _OS_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <semaphore.h>


#define OS_WAIT_FOREVER 0xffffff00 //wait for ever

#define FPI_MAX_OS_NAME_LENGTH   	8 // max name length used for os. eg. semaphore name/mutex name etc.


#define MAX_QUEUE_MESSAGE_BUFFER_SIZE	512 //the max size of each message send to msg queue

// Invalid IDs, Shall be used while initial an id.
#define OS_THREAD_INVALID_ID      -1 //Invalid thread ID
#define OS_MUTEX_INVALID_ID       -1 //Invalid mutex ID
#define OS_SEMAPHORE_INVALID_ID   -1 //Invalid semaphore ID
#define OS_MQUEUE_INVALID_ID      -1 //Invalid msg queue ID
#define OS_EVENT_GROUP_INVALID_ID -1 //Invalid event group ID
#define OS_EVENT_MSGQ_INVALID_ID  -1 //Invalid msg queue ID

// thread priority for normal use.
#define OS_PRIORITY_LOW      40
#define OS_PRIORITY_MIDDLE   50
#define OS_PRIORITY_HIGH     60

#define OS_PRIORITY_MIN  1
#define OS_PRIORITY_MAX  100
#define OS_BUSYBOX_CMD_LEN  32

typedef int int32_t;
typedef unsigned int uint32_t;
typedef unsigned char uint8_t;

typedef int32_t os_thread_handle_t;
typedef int32_t os_mutex_handle_t;
typedef int32_t os_semaphore_handle_t;
typedef int32_t os_event_group_handle_t;
typedef int32_t os_mqueue_handle_t;

#ifndef BT0
#define BT0 \
	do {\
		TRILD_INFO("[%s]: Called by:%p\n", __FUNCTION__, __builtin_return_address(0));\
	}while (0);
#endif

typedef enum _FPI_OS_MSG_ERASE_TYPR_E
{
    eFPI_OS_ERASE_ALL_MSG = 0,
    eFPI_OS_ERASE_ONE_MSG,
    eFPI_OS_ERASE_MAX
}fpi_os_msg_erase_type;

typedef enum _FPI_OS_ERROR_E
{
    eFPI_FAILURE = 0,
    eFPI_ERROR_BAD_PARAMETER,
    eFPI_ERROR_TIMEOUT,
    eFPI_SUCCESS
}fpi_os_error;

typedef enum _OS_EVENTGROUP_OP_MODE_E
{
    EM_EVENTGROUP_OP_MODE_AND = 0,
    EM_EVENTGROUP_OP_MODE_AND_CLEAR,    
    EM_EVENTGROUP_OP_MODE_OR,  
    EM_EVENTGROUP_OP_MODE_OR_CLEAR
}OS_EVENTGROUP_OP_MODE_E;

typedef enum
{
	OS_CMD_TOOLS_DEFAULT,
	OS_CMD_TOOLS_BUSYBOX,
	OS_CMD_TOOLS_TOYBOX,
	OS_CMD_TOOLS_NONE
}OS_CMD_TOOLS_TYPE_E;


// Thread

//#define OS_THREAD_DEBUG
#define COURSE_MAIN_THREAD_ID   0xfffffff0

#ifdef OS_THREAD_DEBUG
#define THREAD_NUM_MAX  200
typedef struct
{
    uint32_t run_counter;
    uint32_t thread_index;
    int32_t  thread_id;
    uint32_t run_stop;
    char name[32];
}system_thread_t;

system_thread_t * os_get_thread_debug_info(void);
uint32_t os_get_thread_counter(void);
extern void os_thread_var_check(os_thread_handle_t id);

#define OS_THREAD_INSERT_INFO(x)    os_thread_var_check(x)

#else
#define OS_THREAD_INSERT_INFO(x) 
#endif


//Entry for each thread.
typedef void *(*function_entry)(void * param);

/******************************************************************************
*Function: os_thread_create
*
*Description: Create a thread.
*
*Input:     function -- thread entry.
*           arg      -- thread arg.
*           stacksize-- stack size for the created thread.
*           priority -- priority for the created thread.
*           name     -- thread name
*
*Output:    NONE
*
*Return:    the handle of the created thread. if failed, OS_THREAD_INVALID_ID
*           will be returned.
*           
*Others:    If the input stacksize = 0, OS_STACK_SIZE_DEFAULT will be used.
*           priority is not working for this moment as the schedule mothed is other.
*           The created thread is DETACHED and it is no need to call join to reclaim stack resource.
******************************************************************************/
os_thread_handle_t os_thread_create(function_entry function, 
                             void *         arg,
                             uint32_t       stacksize, 
                             uint8_t        priority,
                             char *         name);

/******************************************************************************
*Function: os_thread_delete
*
*Description: Delete a thread.
*
*Input:     thread_id -- handle of the thread that need to delete
*
*Output:    NONE
*
*Return:    fpi_true  -- success.
*           fpi_false -- failed.
*           
*Others:    Linux don't support delete a thread. Do nothing this moment.
******************************************************************************/
int os_thread_delete(os_thread_handle_t thread_id);

//int os_thread_start(int32_t thread_id);

//int os_thread_stop(int32_t thread_id);

/******************************************************************************
*Function: os_thread_self
*
*Description: Get the thread id of current thread.
*
*Input:     NONE
*
*Output:    NONE
*
*Return:    thread id of current thread.
*           
*Others:    NONE.
******************************************************************************/
os_thread_handle_t os_thread_self();


//
// Mutex
//
/******************************************************************************
*Function: os_mutex_create
*
*Description: Get the thread id of current thread.
*
*Input:     p_mutex_name -- mutex name
*
*Output:    NONE
*
*Return:    the handle of the created mutex. if failed, OS_MUTEX_INVALID_ID
*           will be returned.
*           
*Others:    NONE.
******************************************************************************/
pthread_mutex_t *os_mutex_create(char *p_mutex_name);

/******************************************************************************
*Function: os_mutex_delete
*
*Description: Delete a mutex
*
*Input:     mutex_id -- handle of the mutex
 *
*Output:    NONE
*
*Return:    fpi_true  -- success.
*           fpi_false -- failed.
*           
*Others:    NONE.
******************************************************************************/
int os_mutex_delete(pthread_mutex_t * mutex_id);

/******************************************************************************
*Function: os_mutex_obtain
*
*Description: obtain a mutex
*
*Input:     mutex_id    -- handle of the mutex
*           n32_wait_ms -- timeout for obtaining this mutex
*
*Output:    NONE
*
*Return:    fpi_true  -- success.
*           fpi_false -- failed.
*           
*Others:    NONE.
******************************************************************************/
int os_mutex_obtain(pthread_mutex_t * mutex_id, int32_t n32_wait_ms);

/******************************************************************************
*Function: os_mutex_release
*
*Description: release a mutex
*
*Input:     mutex_id    -- handle of the mutex
*
*Output:    NONE
*
*Return:    fpi_true  -- success.
*           fpi_false -- failed.
*           
*Others:    NONE.
******************************************************************************/
int os_mutex_release(pthread_mutex_t * mutex_id);

//int os_InfoMutex(int32_t s32MutexId, fpi_OSAttribute *peAttribute, char *pMutexName);


//
// Semaphore
//
/******************************************************************************
*Function: os_semaphore_create
*
*Description: Create a semaphore.
*
*Input:     u32_init_cnt -- the initial value of the semaphore.
*           p_name       -- Created semaphore name.
*
*Output:    NONE
*
*Return:    the handle of the created semaphore. if failed, OS_SEMAPHORE_INVALID_ID
*           will be returned.
*           
*Others:    NONE.
******************************************************************************/
sem_t * os_semaphore_create(uint32_t u32_init_cnt, char *p_name);

/******************************************************************************
*Function: os_semaphore_delete
*
*Description: Delete a semaphore.
*
*Input:     semaphore_id -- handle of the semaphore
*
*Output:    NONE
*
*Return:    fpi_true  -- success.
*           fpi_false -- failed.
*           
*Others:    NONE.
******************************************************************************/
int os_semaphore_delete(sem_t * semaphore_id);

/******************************************************************************
*Function: os_semaphore_wait
*
*Description: Wait for a semaphore.
*
*Input:     semaphore_id -- handle of the semaphore.
*           n32_wait_ms  -- timeout for the semaphore.
*
*Output:    NONE
*
*Return:    fpi_true  -- success.
*           fpi_false -- failed.
*           
*Others:    NONE.
******************************************************************************/
int os_semaphore_wait(sem_t * semaphore_id, int32_t n32_wait_ms);

/******************************************************************************
*Function: os_semaphore_post
*
*Description: post a semaphore.
*
*Input:     semaphore_id -- handle of the semaphore.
*
*Output:    NONE
*
*Return:    fpi_true  -- success.
*           fpi_false -- failed.
*           
*Others:    NONE.
******************************************************************************/
int os_semaphore_post(sem_t * semaphore_id);

//int os_InfoSemaphore (int32_t s32SemaphoreId, uint32_t *pu32Cnt, fpi_OSAttribute *peAttribute, char *pSemaphoreName);


/***************************************************************************/
//typedef int32_t TVHMSG_QueueHandle_t;
/****************************************************************************/
#define TVHMSG_INVALIDATE_QUEUE_HANDLE   (-1)
#define TVHMSG_WAIT_TIME_INFINITE       (-1)
#define TVHMSG_WAIT_TIME_IMMEDIATE       (0)

typedef struct os_mqueue_st
{
	struct os_mqueue_st *handle;
	sem_t *queue_sem;
	sem_t * data_sem;
	uint32_t max_message;
	uint32_t message_size;
	int32_t send_message;
	int32_t write_offset;
	int32_t read_offset;
	int32_t end_offset;
	uint8_t* message_buffer;
	struct os_mqueue_st *next;
}os_mqueue_t;

fpi_os_error os_mqueue_create(os_mqueue_t** message_queue_id,		
						   uint32_t max_messages,	
						   int32_t message_size	
						   );

/*****************************************************************************
Name: tvhmsg_queue_delete
Description:
	delete a message queue
Parameters:
	message_queue_id:the id of message queue which will be deleted
Return Value:
    
See Also:
Author: FuYong    
*****************************************************************************/
fpi_os_error os_mqueue_destroy(os_mqueue_t* message_queue_id);

/*****************************************************************************
Name: MWSDIOS_MessageQueueReset

Description:
	reset a message queue
Parameters:
	message_queue_id:the id of message queue which will be deleted
Return Value:
    
See Also:
Author: FuYong    
*****************************************************************************/
fpi_os_error os_mqueue_reset_message(os_mqueue_t* message_queue_id);
/*****************************************************************************
Name: tvhmsg_queue_send
Description:
	
Parameters:
	message_queue_id:the id of current message queue
	message:the message will be send
Return Value:
    
See Also:
Author: FuYong    
*****************************************************************************/
fpi_os_error os_mqueue_send_message(os_mqueue_t* message_queue_id,		
						 void       *message,
						 uint32_t   millisecond
						 );
/*****************************************************************************
Name: tvhmsg_queue_receive
Description:
	receive the message in one message queue
Parameters:
	message_queue_id:the id of the message queue 
	message:return the message of the queue which have been got
	wait_mode:the mode of current message queue 
	millisecond:the time of the message queue's peroid
Return Value:
    
See Also:
Author: FuYong    
*****************************************************************************/
fpi_os_error os_mqueue_receive_message(os_mqueue_t* message_queue_id,		
							void       *message,						
							uint32_t   millisecond				
							);
/*****************************************************************************
Name: tvhmsg_queue_remove_by_element
Description:
	remove the message by element value
Parameters:
	message_queue_id:the id of the message queue 
	elem_offset:element offset byte in message 
	min_elem_value:the min value of element
	max_elem_value:the max value of element
Return Value:
    
See Also:
Author: FuYong    
*****************************************************************************/
fpi_os_error os_mqueue_remove_by_element(os_mqueue_t* message_queue_id,
                                               uint8_t elem_offset,
                                               uint32_t min_elem_value,
                                               uint32_t max_elem_value);

int32_t os_mqueue_get_message_count(os_mqueue_t* message_queue_id);

//
// System time
//
void os_sleep(uint32_t un_us);

void os_msleep(uint32_t un_ms);

void os_usleep(uint32_t un_us);

/******************************************************************************
*Function: os_get_tickcount
*
*Description: get the past millisecond from the power on of the platform.
*
*Input:     NONE
*
*Output:    NONE
*
*Return:    past time. millisecond.
*           
*Others:    NONE.
******************************************************************************/
uint32_t os_get_tickcount(void);


/******************************************************************************
*Function: os_get_systemtime
*
*Description: get the utc time.
*
*Input:     NONE
*
*Output:    NONE
*
*Return:    utc time. second.
*           
*Others:    NONE.
******************************************************************************/
uint32_t os_get_systemtime(void);

/******************************************************************************
*Function: os_set_systemtime
*
*Description: set the utc time.
*
*Input:     utc_sec - UTC time. Second.
*
*Output:    NONE
*
*Return:    fpi_true -- success
*           fpi_false -- failed
*           
*Others:    NONE.
******************************************************************************/
int os_set_systemtime(uint32_t utc_sec);

#if 1
/******************************************************************************
*Function: os_get_timezone
*
*Description: get the time zone. Minute.
*
*Input:     NONE
*
*Output:    minutes - time zone. Minute.
*
*Return:    fpi_true -- success
*           fpi_false -- failed
*           
*Others:    NONE.
******************************************************************************/
int os_get_timezone(int32_t *minutes);

/******************************************************************************
*Function: os_set_timezone
*
*Description: set the time zone. Minute.
*
*Input:     minutes - time zone. Minute.
*
*Output:    NONE
*
*Return:    fpi_true -- success
*           fpi_false -- failed
*           
*Others:    NONE.
******************************************************************************/
int os_set_timezone(int32_t minutes);
#endif
#define OS_COMMAD_LEN	256

//void os_cmd_sync(void);

/*parameter maybe null*/
void os_cmd_touch(const char * parameter, const char * cmd_str);

void os_cmd_rm(const char * parameter, const char * cmd_str);

void os_cmd_cp(const char * parameter, const char * cmd_str1,const char * cmd_str2);

void os_cmd_mkdir(const char * parameter, const char * cmd_str);

void os_mutex_dump();

void os_mutex_dumplist();

/******************************************************************************
*Function: os_get_cmd_tools_type
*Description: 获取当前系统所用的system命令的工具集
*Input:     NONE
*Output:
*Return:    OS_CMD_TOOLS_NONE-----  no external system cmd tools
		   OS_CMD_TOOLS_BUSYBOX------  use busybox tools
		   OS_CMD_TOOLS_TOYBOX ----- use toybox tools
*Others:
******************************************************************************/
OS_CMD_TOOLS_TYPE_E os_get_cmd_tools_type(void);


/******************************************************************************
*Function: os_cmd_system
*Description: 通过shell方式调用系统命令
*Input:     cmdstring 通过shell输入的字符串
*Output:    0 系统命令操作成功
            -1 系统命令操作失败
*Return:
*Others:
******************************************************************************/
int32_t os_cmd_system(const char *cmdstring);



#ifdef __cplusplus
}
#endif

#endif	//_OS_H_
