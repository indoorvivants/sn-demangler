import sbt.VirtualAxis

sealed abstract class OS_Axis(val idSuffix: String, val directorySuffix: String)
    extends VirtualAxis.WeakAxis

object OS_Axis {
  case object Current extends OS_Axis("-os-default-", "-default-os")
  case object AppleM1 extends OS_Axis("-apple-m1-", "-apple-m1")
}
