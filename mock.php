<?php

$t = 1377280000;
$latini = $lat = 45;
$lonini = $lon = 4;
$altini = $alt = 300;
$acc = 2;



$dlon=0;
$dt = 60;
$da = 0;
for($i = 0; $i < 1000; $i++) {
  $angle = 360 * $i / 24;
  $r = 0.01 * log($i+1);
  $GLOBALS['lat'] = $latini + $r * cos($angle/180*3.1415);
  $GLOBALS['lon'] = $lonini + $r * sin($angle/180*3.1415);
  $GLOBALS['alt'] = $altini + 100 * sin($angle/180*3.1415);
  mock();
}


/*
$dlon=0.01;
$dt = 60;
$da = 10;
for($i = 0; $i < 20; $i++) {
  mock();
}
$da = -20;
for($i = 0; $i < 30; $i++) {
  mock();
}
$da = 10;
for($i = 0; $i < 20; $i++) {
  mock();
}
$da = 0;
for($i = 0; $i < 20; $i++) {
  mock();
}
$da = 10;
for($i = 0; $i < 100; $i++) {
  mock();
}
*/


function mock() {
  echo $GLOBALS['t'].','.$GLOBALS['lat'].','.$GLOBALS['lon'].','.$GLOBALS['alt'].','.$GLOBALS['acc']."\n";
  $GLOBALS['alt'] += $GLOBALS['da'];
  $GLOBALS['t'] += $GLOBALS['dt'];
  $GLOBALS['lon'] += $GLOBALS['dlon'];
}

