//
// Created by zongrong.pan on 2020/9/10.
//

#include <jni.h>
#include <string>
#include <iostream>
#include "include/trild.h"
#include "include/dmsdp_camera_handler.h"
#include "include/mme/zmf_ext.h"
#include <android/log.h>
#include <malloc.h>
#include <string.h>

extern "C"
{
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
//#include "include/libavutil/avassert.h"
//#include "include/libavutil/channel_layout.h"
//#include "include/libavutil/opt.h"
//#include "include/libavutil/mathematics.h"
//#include "include/libavutil/timestamp.h"
//#include "include/libavformat/avformat.h"
}
#include <stdio.h>

#define TRILD_INFO(...) __android_log_print(ANDROID_LOG_INFO  , "Mem_CC_trild", __VA_ARGS__)
#define TRILD_ERROR(...) __android_log_print(ANDROID_LOG_INFO  , "Mem_CC_trild-Erro", __VA_ARGS__)

const uint8_t* ibuffer = NULL;
const uint32_t iLen = 0;
int32_t cb(const char* id, uint32_t idLen, const uint8_t* buffer, uint32_t len, int32_t type)
{
    //TRILD_INFO("Mem_CC DMSDPSendBackDataCB start \n");
    const char* captureid = "ju_h264";//"ju_h264";//"Camera@0";
    int iWidthHeight[2];
    iWidthHeight[0] = 1280;
    iWidthHeight[1] = 720;
    ZmfVideoCaptureEncoder encoderCfg;
    encoderCfg.plName = "H264";
    encoderCfg.bufLength = len;
    encoderCfg.bLastLayer = 1;
    encoderCfg.bKeyFrame = 0;
    encoderCfg.simulcastIdx = 0;
    encoderCfg.fragLevel = 0;
    encoderCfg.rtt = 0;
    encoderCfg.newBitRate = 0;
    encoderCfg.newFrameRate = 0;
    encoderCfg.packetLoss = 0;
    encoderCfg.nextKeyFrame = 0;
    encoderCfg.bLastFrag = 0;
    FILE *fv;
    fv = fopen( "/data/data/com.test.testsample/testvedioplay02" , "ab+" );
    fwrite(buffer,1,len, fv);

    if (ibuffer == NULL)
    {
        ibuffer = static_cast<const uint8_t *>(malloc(40 * 1024));
    }
    //TRILD_INFO("Mem_CC Zmf_OnH264AnnexBCapture start");
    Zmf_OnH264AnnexBCapture(captureid, 0, 0, 0, iWidthHeight, iWidthHeight+1, (unsigned char*)buffer,
                            &encoderCfg);
    if(encoderCfg.bKeyFrame == 1){
        //ibuffer = buffer;
        memset((void *) ibuffer, 0x00, (40 * 1024));
        strcpy((char *) ibuffer, reinterpret_cast<const char *>(buffer));
        //memcpy((void *) ibuffer,buffer, len);
//        TRILD_INFO("Zmf_OnH264AnnexBCapture Get len = %d ibuffer[0] = [%d] ibuffer[1] = [%d] ibuffer[2] = [%d] ibuffer[3] = [%d] ibuffer[4] = [%d] ibuffer[5] = [%d] ibuffer[6] = [%d] ibuffer[7] = [%d]\n",
//               len,ibuffer[0],ibuffer[1],ibuffer[2],ibuffer[3],ibuffer[4],ibuffer[5],ibuffer[6],ibuffer[7]);
    }
    if(encoderCfg.nextKeyFrame == 1){
        encoderCfg.plName = "H264";
        encoderCfg.bufLength = len;
        encoderCfg.bLastLayer = 1;
        encoderCfg.bKeyFrame = 0;
        encoderCfg.simulcastIdx = 0;
        encoderCfg.fragLevel = 0;
        encoderCfg.rtt = 0;
        encoderCfg.newBitRate = 0;
        encoderCfg.newFrameRate = 0;
        encoderCfg.packetLoss = 0;
        encoderCfg.nextKeyFrame = 0;
        encoderCfg.bLastFrag = 0;
        Zmf_OnH264AnnexBCapture(captureid, 0, 0, 0, iWidthHeight, iWidthHeight+1, (unsigned char*)ibuffer,
                                &encoderCfg);
    }

//    TRILD_INFO("Zmf_OnH264AnnexBCapture Get len = %d buffer[0] = [%d] buffer[1] = [%d] buffer[2] = [%d] buffer[3] = [%d] buffer[4] = [%d] buffer[5] = [%d] buffer[6] = [%d] buffer[7] = [%d]\n",
//               len,buffer[0],buffer[1],buffer[2],buffer[3],buffer[4],buffer[5],buffer[6],buffer[7]);
    //TRILD_INFO("Mem_CC Zmf_OnH264AnnexBCapture end");
    return 1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_tclphone_activity_MainActivity_Trild_1SetETH(JNIEnv *env, jobject thiz) {
    // TODO: implement Trild_SetETH()
    TRILD_INFO("Java_com_test_testsample_MainActivity_Trild_1SetETH \n");
    jint sesson = Trild_SetETH();
    return sesson;
}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_tclphone_activity_MainActivity_DMSDPGetOpenCamera(JNIEnv *env, jobject thiz,jstring cameraId) {
    // TODO: implement DMSDPGetOpenCamera()
    printf("Mem_CC Java_com_test_testsample_MainActivity_DMSDPGetOpenCamera \n");
    DMSDPCameraHandler cameraHandler;
    DMSDPGetCameraHandler(&cameraHandler);

    DMSDPCameraParam ca;
    ca.id = "video0";//(char*)(*env).GetStringUTFChars(cameraId, 0);//reinterpret_cast<const char *>(cameraId);
    ca.idLen = strlen("video0");//strlen((char*)(*env).GetStringUTFChars(cameraId, 0));
    ca.height = 720;
    ca.width = 1280;
    ca.fps = 30;
    ca.dataFormat = 1;

    cameraHandler.RegisterSendDataCallback(ca.id , ca.idLen,
                                           cb);
    int result = cameraHandler.OpenCamera(&ca);
    uint32_t len = strlen((char*)(*env).GetStringUTFChars(cameraId, 0));
    TRILD_INFO("DMSDPGetOpenCamera Get id = %s\n", ca.id);
    TRILD_INFO("DMSDPGetOpenCamera Get idLen1 = %d\n", len);
    TRILD_INFO("DMSDPGetOpenCamera Get idLen = %d\n", ca.idLen);

    TRILD_INFO("DMSDPGetOpenCamera Get result = %d\n", result);
    printf("Mem_CC Java_com_test_testsample_MainActivity_DMSDPGetOpenCamera end \n");
    return result;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_tclphone_activity_MainActivity_DMSDPGetCloseCamera(JNIEnv *env, jobject thiz
) {
    // TODO: implement DMSDPGetCloseCamera()
    printf("Mem_CC Java_com_test_testsample_MainActivity_DMSDPGetCloseCamera \n");
    DMSDPCameraHandler cameraHandler;
    DMSDPGetCameraHandler(&cameraHandler);

    DMSDPCameraParam ca;
    ca.id = "video0";//(char*)(*env).GetStringUTFChars(cameraId, 0);//reinterpret_cast<const char *>(cameraId);
    ca.idLen = strlen("video0");//strlen((char*)(*env).GetStringUTFChars(cameraId, 0));
    ca.height = 720;
    ca.width = 1280;
    ca.fps = 30;
    ca.dataFormat = 1;
    cameraHandler.RegisterSendDataCallback(ca.id, ca.idLen,
                                           cb);
    //uint32_t len = strlen((char*)(*env).GetStringUTFChars(camera_id, 0));
    TRILD_INFO("DMSDPGetOpenCamera Get id = %s\n", ca.id);
    //TRILD_INFO("DMSDPGetOpenCamera Get idLen1 = %d\n", len);
    TRILD_INFO("DMSDPGetOpenCamera Get idLen = %d\n", ca.idLen);
    int result = cameraHandler.CloseCamera(ca.id, ca.idLen);
    TRILD_INFO("DMSDPGetOpenCamera Get result = %d\n", result);
    printf("Mem_CC Java_com_test_testsample_MainActivity_DMSDPGetCloseCamera end \n");
    return result;
}
//int av_bsf_filter(const AVBitStreamFilter *filter, AVPacket *pPacket, const AVCodecParameters *src)
//{
//    int ret;
//    AVBSFContext *ctx = NULL;
//    if (!filter)
//        return 0;
//    ret = av_bsf_alloc(filter, &ctx);
//    if (ret < 0)
//        return ret;
//    ret = avcodec_parameters_copy(ctx->par_in, src);
//    if (ret < 0)
//        return ret;
//    ret = av_bsf_init(ctx);
//    if (ret < 0)
//        return ret;
//
//    AVPacket pkt = { 0 };
//    pkt.data = pPacket->data;
//    pkt.size = pPacket->size;
//
//    ret = av_bsf_send_packet(ctx, &pkt);
//    if (ret < 0)
//        return ret;
//
//    ret = av_bsf_receive_packet(ctx, &pkt);
//    if (pkt.data == pPacket->data)//棰濆澶勭悊瑁告祦鏂囦欢  鏍囧噯鏍煎紡 涓嶇敤寰€涓嬮潰璧?鍥犱负pkt.data娌℃湁鏂扮殑寮曠敤銆?
//    {
//        uint8_t *poutbuf = (uint8_t*)av_malloc(pkt.size);
//        if (!poutbuf) {
//            av_packet_unref(&pkt);
//            return -1;
//        }
//        memcpy(poutbuf, pkt.data, pkt.size);
//        av_packet_unref(pPacket);
//        pPacket->data = poutbuf;
//        pPacket->size = pkt.size;
//        av_packet_unref(&pkt);
//        av_bsf_free(&ctx);
//        return 1;
//        //av_packet_unref(pPacket);
//    }
//
//    if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
//        return 0;
//    else if (ret < 0)
//        return ret;
//
//    // 	uint8_t **poutbuf = &(pPacket->data);
//    // 	int *poutbuf_size = &(pPacket->size);
//
//    uint8_t *poutbuf = (uint8_t*)av_malloc(pkt.size + AV_INPUT_BUFFER_PADDING_SIZE);
//    if (!poutbuf) {
//        av_packet_unref(&pkt);
//        return AVERROR(ENOMEM);
//    }
//    int poutbuf_size = pkt.size;
//    memcpy(poutbuf, pkt.data, pkt.size);
//    pPacket->data = poutbuf;
//    pPacket->size = poutbuf_size;
//
//    av_packet_unref(&pkt);
//
//    /* drain all the remaining packets we cannot return */
//    while (ret >= 0) {
//        ret = av_bsf_receive_packet(ctx, &pkt);
//        av_packet_unref(&pkt);
//    }
//
//    av_bsf_free(&ctx);
//    return 1;
//}
//
//    int adjustbreak=0;
//    void* uploadata (){
//
//        av_register_all();
//        adjustbreak=0;
//        const char* captureid = "ju_h264";//"Camera@0";
//        int iWidthHeight[2];
//        iWidthHeight[0] = 1280;//1024;
//        iWidthHeight[1] = 720;//576;
//        ZmfVideoCaptureEncoder encoderCfg;
//        encoderCfg.plName = "H264";
//        encoderCfg.bLastLayer = 1;
//        encoderCfg.bKeyFrame = 0;
//        encoderCfg.simulcastIdx = 0;
//        encoderCfg.fragLevel = 0;
//        encoderCfg.rtt = 0;
//        encoderCfg.newBitRate = 0;
//        encoderCfg.newFrameRate = 0;
//        encoderCfg.packetLoss = 0;
//        encoderCfg.nextKeyFrame = 0;
//        encoderCfg.bLastFrag = 0;
//        //AVFormatContext *pFormatCtx = NULL;
//        //AVPacket pkt;
//        char * filename="rtmp://192.168.0.33:2016/rtmplive";
//        //char * filename="rtmp://202.69.69.180:443/webcast/bshdlive-pc";
//        TRILD_INFO("Hello World!\n");
//
//        AVFormatContext *inputCtx;
//        AVPacket pkt;
//        int ret;
//        TRILD_INFO("Hello World!++++++++++++++++++++\n");
//        //alloc context
//        inputCtx = avformat_alloc_context();
//        TRILD_INFO("Hello World!--------------------\n");
//        //open input
//        ret = avformat_open_input(&inputCtx,filename, NULL, NULL);
//        if(ret != 0){
//            printf("Couldn't open input stream %s\n",filename);
//            exit(1);
//        }
//        TRILD_INFO("Hello World!=======================\n");
//        //get stream info
//        if(avformat_find_stream_info(inputCtx,NULL)<0)
//        {
//            printf("Couldn't find stream information.\n");
//            exit(1);
//        }
//        int videoindex= -1;
//        int audioindex= -1;
//
//        for (int j = 0; j<inputCtx->nb_streams; j++)
//        {
//            //printf("pFormatCtx->streams[j]->codec->codec_type is %d \n",pFormatCtx->streams[j]->codec->codec_type);
//            if (inputCtx->streams[j]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO)
//            {
//                videoindex = j;
//                break;
//            }
//
//        }
//
//        for (int k = 0; k < inputCtx->nb_streams; k++)
//        {
//            //printf("pFormatCtx->streams[j]->codec->codec_type is %d \n",pFormatCtx->streams[j]->codec->codec_type);
//            if (inputCtx->streams[k]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO)
//            {
//                audioindex = k;
//                break;
//            }
//
//        }
//        //FILE *fv;
//        //fv = fopen( "/data/data/com.test.testsample/testvedioplay01" , "ab+" );
//        const AVBitStreamFilter *pavBitStFilter = av_bsf_get_by_name("h264_mp4toannexb");
//
//        while(true)
//        {
//            if(adjustbreak != 0) break;
//            if(av_read_frame(inputCtx, &pkt) >= 0){
//
//                if (pkt.stream_index == audioindex){
//                    //printf("run AVMEDIA_TYPE_AUDIO time is %d and pkt->size is %d,pts is %lld\n",i,pkt->size,pkt->pts);
//                    //fwrite(pkt->data, sizeof(pkt->data) , 1, fa );
//                    //fwrite(pkt->data,1,pkt->size,fa);
//                }
//
//                if (pkt.stream_index == videoindex){
//                    if (av_bsf_filter(pavBitStFilter, &pkt, inputCtx->streams[videoindex]->codecpar) != 1)// 娣诲姞pps sps锛圧TMP闇€瑕侊級
//                    {
//                        /*len = 0;*/
//                        return NULL;
//                    }
//                    TRILD_INFO("Hello World!start Zmf_OnH264AnnexBCapture and pkt.size is %d  pts is %lld\n",pkt.size,pkt.pts);
//                    //fwrite(pkt.data,1,pkt.size, fv);
//                    encoderCfg.bufLength =pkt.size;
//                    Zmf_OnH264AnnexBCapture(captureid, 0, 0, 0, iWidthHeight, iWidthHeight+1, (unsigned char*)pkt.data,
//                                            &encoderCfg);
//                    //fwrite(pkt.data,1,pkt.size, fv);
//                }
//
//                av_packet_unref(&pkt);
//            }
//        }
//        //fclose(fv);
//    return NULL;
//    }
//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_example_tclphone_activity_MainActivity_stratpthead(JNIEnv *env, jobject thiz) {
//    TRILD_INFO("stratpthead start \n");
//    pthread_t tidp;if ((pthread_create(&tidp, NULL, reinterpret_cast<void *(*)(void *)>(uploadata), NULL)) == -1){TRILD_INFO("create error!\n");return 1;}
//    //uploadata();
//    return 0;
//    }
//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_example_tclphone_activity_MainActivity_stoppthead(JNIEnv *env, jobject thiz) {
//    TRILD_INFO("Java_com_test_testsample_MainActivity_stoppthead ----------------------------------------------- \n");
//    adjustbreak=1;
//    return 0;
//    }
//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_example_tclphone_fragment_audioFragment_stratpthead(JNIEnv *env, jobject thiz) {
//    // TODO: implement stratpthead()
//    TRILD_INFO("stratpthead start \n");
//    pthread_t tidp;if ((pthread_create(&tidp, NULL, reinterpret_cast<void *(*)(void *)>(uploadata), NULL)) == -1){TRILD_INFO("create error!\n");return 1;}
//    //uploadata();
//    return 0;
//}extern "C"
//JNIEXPORT jint JNICALL
//Java_com_example_tclphone_fragment_audioFragment_stoppthead(JNIEnv *env, jobject thiz) {
//    // TODO: implement stoppthead()
//    TRILD_INFO("Java_com_test_testsample_MainActivity_stoppthead ----------------------------------------------- \n");
//    adjustbreak=1;
//    return 0;
//}extern "C"
//JNIEXPORT jint JNICALL
//Java_com_example_tclphone_activity_TclCallActivity_stoppthead(JNIEnv *env, jobject thiz) {
//    // TODO: implement stoppthead()
//    TRILD_INFO("Java_com_test_testsample_MainActivity_stoppthead ----------------------------------------------- \n");
//    adjustbreak=1;
//    return 0;
//}
//extern "C"
//JNIEXPORT jstring JNICALL
//Java_com_tcl_tv5g_AutoLogin_Trild_1GetnInPCSCFAddr(JNIEnv *env, jobject thiz,
//                                                   jstring n_in_pcscfaddrc,
//                                                   jstring prefix_length_ipaddrc) {
//
//    // TODO: implement Trild_GetnInPCSCFAddr()
//    TRILD_INFO("Mem_CC Java_com_test_testsample_MainActivity_Trild_1GetnInPCSCFAddr");
//    char nTemp[60]={0};
//    int nTemp2;
//    jint sesson =Trild_GetnInPCSCFAddr(nTemp,
//                                       &nTemp2);
//    //TRILD_INFO("Trild_GetnImsIpAddr Get n_ims_ip_addr = %s\n", n_ims_ip_addr);
//    TRILD_INFO("Trild_GetnInPCSCFAddr Get nTemp = %s\n", nTemp);
//    TRILD_INFO("Trild_GetnInPCSCFAddr Get nTemp2 = %d\n", nTemp2);
//    TRILD_INFO("Trild_GetnInPCSCFAddr Get sesson = %d\n", sesson);
////    env->ReleaseStringChars((jstring) n_ims_ip_addrc, (jchar *) nTemp);
////    env->ReleaseStringChars((jstring) prefix_length_ipaddrc, (jchar *) nTemp2);
//    if (sesson == 1) {
//        TRILD_INFO("Trild_GetnInPCSCFAddr Get sesson = %d\n", sesson);
//        return NULL;
//    }else{
//        TRILD_INFO("Trild_GetnInPCSCFAddr Get2 sesson2 = %d\n", sesson);
//        printf("Mem_CC Java_com_test_testsample_MainActivity_Trild_1GetnInPCSCFAddr end \n");
//        jstring result = env->NewStringUTF(nTemp);
//        return result;//reinterpret_cast<jstring>(nTemp);
//    }
//
//
//}

 extern "C"
 JNIEXPORT jstring JNICALL
 Java_com_example_tclphone_AutoLogin_Trild_1GetnInPCSCFAddr(JNIEnv *env, jobject thiz,
                                                           jstring n_in_pcscfaddrc,
                                                           jstring prefix_length_ipaddrc) {
    // TODO: implement Trild_GetnInPCSCFAddr()

    TRILD_INFO("Mem_CC Java_com_test_testsample_MainActivity_Trild_1GetnInPCSCFAddr");
    char nTemp[60]={0};
    int nTemp2;
    jint sesson =Trild_GetnInPCSCFAddr(nTemp,
                                       &nTemp2);
    //TRILD_INFO("Trild_GetnImsIpAddr Get n_ims_ip_addr = %s\n", n_ims_ip_addr);
    TRILD_INFO("Trild_GetnInPCSCFAddr Get nTemp = %s\n", nTemp);
    TRILD_INFO("Trild_GetnInPCSCFAddr Get nTemp2 = %d\n", nTemp2);
    TRILD_INFO("Trild_GetnInPCSCFAddr Get sesson = %d\n", sesson);
//    env->ReleaseStringChars((jstring) n_ims_ip_addrc, (jchar *) nTemp);
//    env->ReleaseStringChars((jstring) prefix_length_ipaddrc, (jchar *) nTemp2);
    if (sesson == 1) {
        TRILD_INFO("Trild_GetnInPCSCFAddr Get sesson = %d\n", sesson);
        return NULL;
    }else {
        TRILD_INFO("Trild_GetnInPCSCFAddr Get2 sesson2 = %d\n", sesson);
        printf("Mem_CC Java_com_test_testsample_MainActivity_Trild_1GetnInPCSCFAddr end \n");
        jstring result = env->NewStringUTF(nTemp);
        return result;//reinterpret_cast<jstring>(nTemp);
    }
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_tclphone_AutoLogin_Trild_11GetnImsIpAddr(JNIEnv *env, jobject thiz,
                                                          jstring n_in_pcscfaddrc,
                                                          jstring n_in_pcscfaddrc_len) {
    // TODO: implement Trild_1GetnImsIpAddr()
    TRILD_INFO("\n Mem_CC Java_com_test_testsample_MainActivity_Trild_1GetnImsIpAddr \n");
    char nTemp[60]={0};
    //char nTemp2[1024]={0};
    int nTemp2;
    jint sesson =Trild_GetnImsIpAddr(nTemp,
                                     &nTemp2);
    //TRILD_INFO("Trild_GetnImsIpAddr Get n_ims_ip_addr = %s\n", n_ims_ip_addr);
    TRILD_INFO("Trild_GetnImsIpAddr Get nTemp = %s\n", nTemp);
    TRILD_INFO("Trild_GetnImsIpAddr Get nTemp2 = %d\n", nTemp2);
    TRILD_INFO("Trild_GetnImsIpAddr Get sesson = %d\n", sesson);
//    env->ReleaseStringChars((jstring) n_ims_ip_addrc, (jchar *) nTemp);
//    env->ReleaseStringChars((jstring) prefix_length_ipaddrc, (jchar *) nTemp2);

    char *nTemp1= static_cast<char *>(malloc(nTemp2));
    //char nTemp2[1024]={0};

    sesson =Trild_GetnImsIpAddr(nTemp1,
                                &nTemp2);
    //TRILD_INFO("Trild_GetnImsIpAddr Get n_ims_ip_addr = %s\n", n_ims_ip_addr);
    TRILD_INFO("Trild_GetnImsIpAddr Get nTemp1 = %s\n", nTemp1);
    TRILD_INFO("Trild_GetnImsIpAddr Get nTemp2 = %d\n", nTemp2);
    TRILD_INFO("Trild_GetnImsIpAddr Get sesson = %d\n", sesson);
    if (sesson == 1) {
        //TRILD_INFO("Trild_GetnImsIpAddr Get sesson = %s\n", sesson);
        return NULL;
    }else{
        //TRILD_INFO("Trild_GetnImsIpAddr Get2 sesson2 = %s\n", sesson);
        free(nTemp1);
        //jint result = (jint) sesson; //void *与jlong之间可强制互转
        printf("Mem_CC Java_com_test_testsample_MainActivity_Trild_1GetnImsIpAddr end \n");
        jstring result;
        result = env->NewStringUTF(nTemp1);
        return result;//reinterpret_cast<jstring>(nTemp);
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_tclphone_AutoLogin_Trild_1GetThreeParameters(JNIEnv *env, jobject thiz) {

    TRILD_INFO("Trild_GetInformation Get start------------\n");
    // TODO: implement Trild_GetThreeParameters()
    char nTemp1[120]={0};
    char nTemp2[120]={0};
    char nTemp3[120]={0};
    jint sesson =Trild_GetInformation(nTemp1,nTemp2,nTemp3);
    //TRILD_INFO("Trild_GetnImsIpAddr Get n_ims_ip_addr = %s\n", n_ims_ip_addr);
    TRILD_INFO("Trild_GetInformation Get nTemp = %s\n", nTemp1);
    TRILD_INFO("Trild_GetInformation Get nTemp2 = %s\n", nTemp2);
    TRILD_INFO("Trild_GetInformation Get sesson = %s\n", nTemp3);
    TRILD_INFO("Trild_GetInformation Get sesson = %d\n", sesson);
//    env->ReleaseStringChars((jstring) n_ims_ip_addrc, (jchar *) nTemp);
//    env->ReleaseStringChars((jstring) prefix_length_ipaddrc, (jchar *) nTemp2);
    if (sesson == 1) {
        TRILD_INFO("Trild_GetInformation Get sesson = %d\n", sesson);
        return NULL;
    }else {
        TRILD_INFO("Trild_GetInformation Get2 sesson2 = %d\n", sesson);

        jstring result1 = env->NewStringUTF(nTemp1);
        TRILD_INFO("Trild_GetInformation Get2 result1 = %s\n", nTemp1);
        jstring result2 = env->NewStringUTF(nTemp2);
        TRILD_INFO("Trild_GetInformation Get2 result2 = %s\n", nTemp2);
        jstring result3 = env->NewStringUTF(nTemp3);
        TRILD_INFO("Trild_GetInformation Get2 result3 = %s\n", nTemp3);


        jclass three_ArrayList = env->FindClass("java/util/ArrayList");

        jmethodID construct = env->GetMethodID(three_ArrayList,"<init>","()V");

        jobject jArrayListObject = env->NewObject(three_ArrayList,construct);

        jmethodID add_ArrayList = env->GetMethodID(three_ArrayList,"add","(Ljava/lang/Object;)Z");

        env->CallBooleanMethod(jArrayListObject,add_ArrayList,result1);

        env->CallBooleanMethod(jArrayListObject,add_ArrayList,result2);

        env->CallBooleanMethod(jArrayListObject,add_ArrayList,result3);


        return jArrayListObject;//reinterpret_cast<jstring>(nTemp);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_tclphone_broadcastreceiver_BootBroadcastReceiver_Trild_1Set_1Network_1Up(
        JNIEnv *env, jobject thiz) {
    // TODO: implement Trild_Set_Network_Up()

    jint sesson =Trild_Set_Network_Up();
}



extern "C"
JNIEXPORT void JNICALL
Java_com_example_tclphone_utils_NativeMethodList_TrildCheck(JNIEnv *env, jclass clazz) {
    // TODO: implement TrildCheck()
    jint sesson =Trild_Note_Application_start();
}