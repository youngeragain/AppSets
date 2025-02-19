import 'package:flutter/cupertino.dart';

class StartPage extends StatefulWidget{
  const StartPage({super.key});


  @override
  State createState() {
    return StartPageState();
  }
}

class StartPageState extends State<StartPage>{

  @override
  Widget build(BuildContext context) {
    return const Text("Start Page");
  }
}