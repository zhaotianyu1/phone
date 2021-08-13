/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 * Description: dmsdp business
 * Author: dongyin
 * Create: 2019-09-06
 */

#ifndef DMSDP_BUSINESS_H
#define DMSDP_BUSINESS_H

#include <stdint.h>

enum {
    DMSDP_NO_ERROR = 0,
    DMSDP_ERR_UNKNOWN_ERROR = -1,
    DMSDP_ERR_INVALID_PARAMETER = -2,
    DMSDP_ERR_NO_MEMORY = -3,
    DMSDP_ERR_INVALID_OPERATION = -4,
    DMSDP_ERR_BAD_VALUE = -5,
    DMSDP_ERR_BAD_TYPE = -6,
    DMSDP_ERR_ENTRY_EXIST = -7,
    DMSDP_ERR_ENTRY_NOEXITS = -8,
    DMSDP_ERR_NOTSUPPORT = -9,

    /* dmsdp camera error */
    DMSDP_ERR_CAMERA_BASE = -1000,

    /* Open camera failed */
    DMSDP_ERR_CAMERA_OPEN_ERROR = -1001,

    /* Close camera failed */
    DMSDP_ERR_CAMERA_CLOSE_ERROR = -1002,

    /* capture preview frame error */
    DMSDP_ERR_CAMERA_CAPTURE_ERROR = -1003,

    /* config camera error, such as set the fps */
    DMSDP_ERR_CAMERA_CONFIG_ERROR = -1004,

    /* control camera error, such as control camera zoom and movement */
    DMSDP_ERR_CAMERA_CONTROL_ERROR = -1005,

    /* dmsdp audio error */
    DMSDP_ERR_AUDIO_BASE = -2000,

    /* dmsdp gps error */
    DMSDP_ERR_GPS_BASE = -3000,
    DMSDP_ERR_GPS_DATA_INVALID_FREQ_PARA = -3001,

    /* dmsdp display error */
    DMSDP_ERR_DISLAY_BASE = -4000,

    /* dmsdp remote control error */
    DMSDP_ERR_REMOTECONTROL_BASE = -5000,

    /* dmsdp codec error */
    ERR_AENC_INVALID_ABILITY_BUFFER_COUNT = -6001,
    ERR_AENC_INVALID_CAHNNEL = -6002,
    ERR_AENC_UNSUPPORT_CODEC = -6003,
    ERR_AENC_UNSUPPORT_PCM_STORE_TYPE = -6004,
    ERR_AENC_UNSUPPORT_SAMPLE_DEPTH = -6005,
    ERR_AENC_UNSUPPORT_SAMPLE_RATE = -6006,
    ERR_AENC_INVALID_BITRATE = -6007,

    ERR_ADEC_INVALID_ABILITY_BUFFER_COUNT = -7001,
    ERR_ADEC_INVALID_CAHNNEL = -7002,
    ERR_ADEC_UNSUPPORT_CODEC = -7003,
    ERR_ADEC_UNSUPPORT_PCM_STORE_TYPE = -7004,
    ERR_ADEC_UNSUPPORT_SAMPLE_DEPTH = -7005,
    ERR_ADEC_UNSUPPORT_SAMPLE_RATE = -7006,
    ERR_ADEC_INVALID_BITRATE = -7007,

    ERR_VENC_INVALID_ABILITY_BUFFER_COUNT = -8001,
    ERR_VENC_UNSUPPORT_CODEC = -8002,
    ERR_VENC_UNSUPPORT_RESOLUTION = -8003,
    ERR_VENC_UNSUPPORT_PROFILE = -8004,
    ERR_VENC_UNSUPPORT_COLOR_FORMAT = -8005,
    ERR_VENC_INVALID_BITRATE = -8006,
    ERR_VENC_TIMESTAMP_INVALID = -8007,

} DMSDPErrorCode;

typedef enum { MSDP_BUSINESS_CTRL_RESERVED = 0 } DMSDPBusinessControlCmd;

typedef enum {
    PLUGIN = 1,
    UNPLUG,
    AVAILABLE,
    UNAVAILABLE,
} DMSDPServiceStatus;

typedef enum {
    /* audio focus change */
    AUDIO_FOCUS_CHANGE = 1,
    /* add camera service info, using DMSDPCameraCapabilities struct */
    CAMERA_ADD_SERVICE = 2,
    /* add camera service info, using DMSDPCameraCapabilities struct json string */
    CAMERA_ADD_SERVICE_STRING = 3,
    /* add audio service info, using DMSDPAudioCapabilities struct */
    AUDIO_ADD_SERVICE = 4,
    /* add audio service info, using DMSDPAudioCapabilities struct json string */
    AUDIO_ADD_SERVICE_STRING = 5
} DMSDPServcieActionType;

typedef struct {
    DMSDPServcieActionType type;
    void* value;
    uint32_t valLen;
} DMSDPServiceAction;

typedef struct {
    int32_t (*UpdateServiceStatus)(const char* id, uint32_t idLen, const DMSDPServiceStatus status);

    int32_t (*UpdateServiceAction)(const char* id, uint32_t idLen, DMSDPServiceAction* action);
} DMSDPListener;

#endif
