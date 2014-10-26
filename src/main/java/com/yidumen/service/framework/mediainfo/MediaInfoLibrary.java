package com.yidumen.service.framework.mediainfo;

import com.sun.jna.FunctionMapper;
import com.sun.jna.Library;
import static com.sun.jna.Library.OPTION_FUNCTION_MAPPER;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.yidumen.service.VideoService;
import java.lang.reflect.Method;
import static java.util.Collections.singletonMap;

/**
 *
 * @author 蔡迪旻
 */
public interface MediaInfoLibrary extends Library {

    MediaInfoLibrary INSTANCE = (MediaInfoLibrary) Native.loadLibrary(VideoService.Library,
                                                                      MediaInfoLibrary.class);

    //Constructor/Destructor
    Pointer MediaInfo();

    void Delete(Pointer Handle);

    //File
    int Open(Pointer Handle, WString file);

    int Open_Buffer_Init(Pointer handle, long length, long offset);

    int Open_Buffer_Continue(Pointer handle, byte[] buffer, int size);

    long Open_Buffer_Continue_GoTo_Get(Pointer handle);

    int Open_Buffer_Finalize(Pointer handle);

    void Close(Pointer Handle);

    //Infos
    WString Inform(Pointer Handle, int Reserved);

    WString Get(Pointer Handle, int StreamKind, int StreamNumber, WString parameter, int infoKind, int searchKind);

    WString GetI(Pointer Handle, int StreamKind, int StreamNumber, int parameterIndex, int infoKind);

    int Count_Get(Pointer Handle, int StreamKind, int StreamNumber);

    //Options
    WString Option(Pointer Handle, WString option, WString value);
}
