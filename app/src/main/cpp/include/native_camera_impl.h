//
//

#ifndef DMSDPNATIVECAMERA_C_API_NATIVE_CAMERA_IMPL_H
#define DMSDPNATIVECAMERA_C_API_NATIVE_CAMERA_IMPL_H

#include <vector>
#include <string>

#include "log.h"
#include "dmsdp_camera_data_type.h"

typedef struct {
    std::vector<DMSDPFpsRange> supportFpsRanges;
    std::vector<DMSDPSize> supportSizes;
    DMSDPCameraDataType dataType;
} DMSDPCameraAbilityInner;

typedef struct {
    std::string key;
    std::string value;
} DMSDPExtendInfoInner;

typedef struct {
    std::string id;
    std::vector<DMSDPCameraAbilityInner> abilities;

    std::vector<DMSDPExtendInfoInner> extendInfos;
} DMSDPCameraCapabilitiesInner;

extern std::string currId;

#endif //DMSDPNATIVECAMERA_C_API_NATIVE_CAMERA_IMPL_H