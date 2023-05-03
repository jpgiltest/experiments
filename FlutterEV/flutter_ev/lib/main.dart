import 'dart:async';

import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.red,
      ),
      home: const MyHomePage(title: 'This ids the <3 best app ever made'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}



class _MyHomePageState extends State<MyHomePage> {
  
  late LatLng currentLatLng = const LatLng(48.555, 2.3333);
  final Completer<GoogleMapController> _controller = Completer();

  Future<void> _getCurrentLocation() async {
    Position position = await Geolocator.getCurrentPosition(desiredAccuracy: LocationAccuracy.high);
    setState(() {
      currentLatLng = LatLng(position.latitude, position.longitude);
    });
    
  }

  @override
  void initState() {
    super.initState();
    _getCurrentLocation();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold (
        body: GoogleMap (
          mapType: MapType.normal,
          initialCameraPosition: CameraPosition(target: currentLatLng, zoom: 14),
          onMapCreated: (GoogleMapController controller) {
            _controller.complete(controller);
          },
          markers: <Marker>{
            Marker(
              draggable: true,
              markerId: const MarkerId("1"),
              position: currentLatLng,
              icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueRed),
              infoWindow: const InfoWindow(title: "You are here")
            )
          }
        ),
        floatingActionButton: FloatingActionButton.extended(
          onPressed: _goToCurrentLocation, 
          label: const Text("Home"),
          icon: const Icon(Icons.home),
      ),
    )
    );
  }

  Future<void> _goToCurrentLocation() async {
    final GoogleMapController controller = await _controller.future;
    controller.animateCamera(CameraUpdate.newCameraPosition(CameraPosition(target: currentLatLng, zoom: 14)));
  }  

}

 