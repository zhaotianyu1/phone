/*******************************************************
 *            COPYRIGHT 2019  TCL                                            *
 *******************************************************
 *
 * MODULE NAME: Trild
 *
 * FILE NAME:   Trild.h
 *              Date: 2020-03-11 
 *              Rev: 
 *
 * PUBLIC
 *
 * DESCRIPTION: Trild Core module
 *
 ********************************************************/

#ifndef _TRILD_H_
#define _TRILD_H_



/*******************************************************/
/*              Includes                               */
/*******************************************************/
#ifdef __cplusplus
extern "C" {
#endif

/*******************************************************/
/*              Defines                                */
/*******************************************************/
#define COMMAND_DELIMITER "|"
#define COMMAND_DELIMITER_CHAR '|'
#define TRILD_NOTE_MAX_SIZE 1024

#define TRILDi_BUF_LEN 1000
#define DEFAULT_DEV "/dev/ttyUSB3"

/*******************************************************/
/*              Macros                                 */
/*******************************************************/


/*******************************************************/
/*              Typedefs                               */
/*******************************************************/

typedef enum
{
    eTRILD_NOTE_NONE,           //none
    eTRILD_NOTE_CHECK_AKA,      //Trild note is connecting
    eTRILD_NOTE_GET_USER_INFO,  //GET impu impi domin
    eTRILD_NOTE_GET_IMS_IP_ADDR, //get IMS channel ip addr
    eTRILD_NOTE_GET_PCSCF_ADDR,  //get pcscf addr
    eTRILD_NOTE_SET_ETH,        //SET_ETH
    eTRILD_NOTE_CHECK_IMS_PROTOCOL,//check ims protocol type, we need to disable ims status
    eTRILD_NOTE_APPLICATION_START,//to note Trild,that 5G application is start.
    eTRILD_NOTE_NETWORK_UP,      //to note Trild to start eth1 up.
    eTRILD_NOTE_ERROR           //Trild note is error
}eTRILD_NOTE_t;


typedef enum
{
    eTRILD_MODEM_NOTE_NONE,        //none
    eTRILD_MODEM_NOTE_SIM_IN,      //Modem send sim card in note to Trild
    eTRILD_MODEM_NOTE_SIM_OUT,     //Modem send sim card out note to Trild
    eTRILD_MODEM_NOTE_IMS_IP_UPDATE,//Modem got ims channel address ip and send it to Trild 
    eTRILD_MODEM_NOTE_PCSCF_UPDATE,//Modem got p-cscf address and send it to Trild 
    eTRILD_MODEM_NOTE_QUECTEL_COMPLETED,//Modem note Trild the quectel-cm is completed 
    eTRILD_MODEM_NOTE_NETWORK_CHANGE,  //Modem note Trild the quectel-cm is network is changed 
    eTRILD_MODEM_NOTE_ERROR        //Modem note is error
}eTRILD_MODEM_NOTE_t;


    
typedef enum
{
    eTRILD_BORADCAST_NONE = 0,
    eTRILD_BORADCAST_PCSCF_UPDATE,
    eTRILD_BORADCAST_IMS_NETWORK_DOWN,
    eTRILD_BORADCAST_IMS_NETWORK_UP,
    eTRILD_BORADCAST_IMS_NETWORK_CHANGE,
    eTRILD_BORADCAST_SIM_OUT,
    eTRILD_BORADCAST_SIM_IN,
    eTRILD_BORADCAST_START_TCL_PHONE,
    eTRILD_BORADCAST_ERROR
}eTRILD_BORADCAST;



/*******************************************************/
/*              Variables Declarations (IMPORT)        */
/*******************************************************/


/*******************************************************/
/*              Functions Declarations                 */
/*******************************************************/

/******************************************************************************
 * Function Name : Trild_CheckAKA
 *
 * Description   : This function is set Auth,Rand to modem, 
 *                 and get res,ck ik from it
 *
 * Side effects  : 
 *
 * Comment       : 
 *
 *****************************************************************************/
 
int Trild_CheckAKA(char *AUTN, char *RAND, char *res, char *ck, char *ik);


int Trild_GetInformation(char *impi, char *impu, char *domain);
int Trild_GetnImsIpAddr(char *nImsIpAddr ,int *PrefixLengthIPAddr);
int Trild_GetnInPCSCFAddr(char *nInPCSCFAddr ,int *PrefixLengthIPAddr);
int Trild_SetETH(void);
int Trild_Set_Network_Up(void);
int Trild_Note_Application_start(void);




#ifdef __cplusplus
}
#endif


#endif 





