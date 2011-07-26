package libhidapi;

import org.bridj.BridJ;
import org.bridj.CRuntime;
import org.bridj.Pointer;
import org.bridj.ann.Library;
import org.bridj.ann.Ptr;
import org.bridj.ann.Runtime;

@Library("hidapi")
@Runtime(CRuntime.class)
public class HidapiLibrary {
  static {
    BridJ.register(HidapiLibrary.class);
  }

  public static native Pointer<hid_device_info> hid_enumerate(short vendor_id, short product_id);

  public static native void hid_free_enumeration(Pointer<hid_device_info> devs);

  public static native Pointer<HidapiLibrary.hid_device> hid_open(short vendor_id, short product_id, Pointer<Character> serial_number);

  public static native Pointer<HidapiLibrary.hid_device> hid_open_path(Pointer<Byte> path);

  public static native int hid_write(Pointer<HidapiLibrary.hid_device> device, Pointer<Byte> data, @Ptr long length);

  public static native int hid_read(Pointer<HidapiLibrary.hid_device> device, Pointer<Byte> data, @Ptr long length);

  public static native int hid_set_nonblocking(Pointer<HidapiLibrary.hid_device> device, int nonblock);

  public static native int hid_send_feature_report(Pointer<HidapiLibrary.hid_device> device, Pointer<Byte> data, @Ptr long length);

  public static native int hid_get_feature_report(Pointer<HidapiLibrary.hid_device> device, Pointer<Byte> data, @Ptr long length);

  public static native void hid_close(Pointer<HidapiLibrary.hid_device> device);

  public static native int hid_get_manufacturer_string(Pointer<HidapiLibrary.hid_device> device, Pointer<Character> string, @Ptr long maxlen);

  public static native int hid_get_product_string(Pointer<HidapiLibrary.hid_device> device, Pointer<Character> string, @Ptr long maxlen);

  public static native int hid_get_serial_number_string(Pointer<HidapiLibrary.hid_device> device, Pointer<Character> string, @Ptr long maxlen);

  public static native int hid_get_indexed_string(Pointer<HidapiLibrary.hid_device> device, int string_index, Pointer<Character> string, @Ptr long maxlen);

  public static native Pointer<Character> hid_error(Pointer<HidapiLibrary.hid_device> device);

  // / Undefined type
  public static interface hid_device {

  };
}