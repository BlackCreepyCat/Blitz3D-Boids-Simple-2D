Const NORMAL =1
Const ALERT = 1
Const COLLIDED = 2

Global drawn ;a global variable simply to determine if the territorial "bind" area has been drawn

;meet bob - a behavioural object
Type bob
	Field x#
	Field y#
	Field heading#
	Field rotation#
	Field speed#
	Field state
	Field wander
	Field Bind
	Field avoid
End Type

;draw a bob to the screen
Function drawbob(abob.bob)
	Local x#=abob\x
	Local y#=abob\y
	Local heading=abob\heading
	Local sh#=10*Sin(heading)
	Local ch#=10*Cos(heading)
	
	If abob\state=ALERT Then
		Color 255,255,100
	Else If abob\state = NORMAL
		Color 0,255,0
	Else If	abob\state=COLLIDED
		Color 255,0,0
	End If
	
	Oval( x-10,y-10,20,20,1)
	Color 255,255,255
	Line x,y,x+1*sh,y-1*ch
	
	;left side of sensor box
	sx1=x-1*ch
	sy1=y-1*sh
	
	sx2=sx1+6*sh
	sy2=sy1-6*ch
	
	;right side of sensorbox
	sx3=x+1*ch
	sy3=y+1*sh

	sx4=sx3+6*sh
	sy4=sy3-6*ch
	
	Line sx1,sy1,sx2,sy2
	Line sx2,sy2,sx4,sy4
	Line sx4,sy4,sx3,sy3
	Oval x-15,y-15,30,30,0
End Function

;rotate a bob by an specified ammount
Function rotatebob(abob.bob,ammount#)
	abob\heading = abob\heading + ammount
	If abob\heading>=360 Then abob\heading=abob\heading-360
	If abob\heading<0 Then abob\heading = 360+abob\heading
End Function

;move a bob by its speed relative to its current heading
Function movebob(abob.bob)
	rotatebob(abob,abob\rotation)
	
	x#=abob\x
	y#=abob\y
	
	heading = abob\heading
	
	x=x+abob\speed*Sin(heading)
	y=y-abob\speed*Cos(heading)
	
	abob\x=x
	abob\y=y
End Function

;randomly alter the bobs turn rate to change its heading periodicaly
Function wanderbob(abob.bob)
	If Not abob\wander Then Return
	a=Rand(10)
	If a<=3 Then abob\rotation=abob\rotation+1
	If a>=8 Then abob\rotation=abob\rotation-1
	If abob\rotation>3 Then abob\rotation=3
	If abob\rotation<-3 Then abob\rotation=-3
	
End Function

;"bind" a bob to a rectangular area
;this simulates a desire for a bob to remain close to or within this area
Function Bindbob (abob.bob,ox,oy,width,height)
	
	Local x#=abob\x
	Local y#=abob\y
	Local heading = abob\heading
	Local rotation#= abob\rotation
	
	If Not drawn Then
		Color 128,128,128
		Rect ox,oy,width,height,0
		Color 255,255,255
		drawn=1
	End If
	
	If Not abob\Bind Then Return
	
	b_left=ox
	b_right=ox+width
	b_top=oy+height
	b_bottom=oy
	
	If x<b_left Then
		If y<b_bottom Then
			If heading <=90 rotation=rotation +1
				If heading >315 rotation=rotation+1
					
					If heading >180 And heading <=315 Then rotation = rotation -1
					
		Else If y>b_top Then
			
			If heading >90 And heading <225 Then rotation = rotation -1
			If heading >225  Then rotation = rotation +1
			
		Else 
			
			If heading>180 And heading <270 Then rotation=rotation-1
			If heading>=270 Then rotation = rotation +1
			
		End If
	End If

	If x>b_right Then
		
		If y<b_bottom Then
			
			If heading <45 Or heading >=270 Then rotation = rotation -1
			If heading >=45 And heading <=180 Then rotation = rotation +1
			
		Else If y>b_top Then
			
			If heading <135 Then rotation = rotation -1
			If heading >=135 And heading <270 Then rotation = rotation +1
			
		Else
			
			If heading >90 And heading <=180 Then rotation = rotation +1
			If heading <=90 Then rotation =rotation -1
			
		End If
		
	End If

	If x<b_right And x>b_left Then
		
		If y>b_top Then
			If heading >45 And heading <=180 Then rotation = rotation -1
			If heading >180 And heading <=315 Then rotation =rotation +1
		End If

		If y<b_bottom Then
			If heading <135 Then rotation = rotation +1
			If heading >225 Then rotation = rotation -1
		End If
		
	End If

	
	If rotation<-3 Then rotation=-3
	If rotation>3 Then rotation=3
	abob\rotation=rotation
End Function

;bobs want to travel in a straight line so
;if they are not continually steered they will "straighten up"
Function dampbob(abob.bob)
	rot#=abob\rotation
	If abob\speed>1.5 Then abob\speed=1.5
	If abob\speed>1 Then abob\speed=abob\speed-0.1
	If abob\speed<0.1 Then abob\speed=0.1
	If rot <0 Then rot=rot+0.1
	If rot >0 Then rot=rot-0.1
	If Abs(rot)<0.1 Then rot = 0
	abob\rotation=rot
	
End Function

;make a bob follow another bob
Function seekBob(predator.bob, prey.bob)
	Local x#=predator\x
	Local y#=predator\y
	Local heading#=predator\heading
	Local rotation#=predator\rotation
	Local tx#,ty#

	tx=prey\x-x
	ty=prey\y-y
	tx2=tx*Cos(180-heading)-ty*Sin(180-heading)
	ty2=tx*Sin(180-heading)+ty*Cos(180-heading)
	If tx2<0 Then rotation=rotation+1
	If tx2>0 Then rotation=rotation-1

	If (tx2^2+ty2^2)>900 Then
		predator\speed=predator\speed +0.1
	Else
		predator\speed=predator\speed -0.1
	End If


	predator\rotation=rotation

End Function

;bobs hate running into other bobs as it gives them a red face
Function avoidbob(abob.bob)

	Local x#=abob\x
	Local y#=abob\y
	Local heading#=abob\heading
	Local rotation#=abob\rotation
	Local tx#,ty#
	Local avoiding.bob
	;transform the coords of each bob in to local bobspace
	avoiding=Null
	count=count+1
	For a.bob=Each bob
		If a<>abob Then
			
			tx#=(a\x-x)
			ty#=(a\y-y)
			;tx and ty now represent "a.bob" relative to abob (abobs position is now the origin)
			;rotate this point around the origin
			tx2=tx*Cos(180-heading)-ty*Sin(180-heading)
			ty2=tx*Sin(180-heading)+ty*Cos(180-heading)
			
			If tx2>=-30 And tx2<=30 And ty2>=-30 And ty2<=70 Then
				
				abob\state=ALERT
				If Sqr(tx2^2+ty2^2) <20 Then abob\state=COLLIDED
				If tx2<0 And ty2>0 Then rotation =rotation-1
				If tx2>=0 And ty2>0 Then rotation = rotation +1
				;if a bob is avoiding another bob it is focused on its task
				;and ignores all other stimulii
				abob\wander=0
				abob\Bind=0
				;abob\seek=0
				avoiding=a
				If ty2>0 Then
					abob\speed=abob\speed-0.1
				Else
					abob\speed=abob\speed+0.1
				End If
			End If
		End If
		;left alone a bob will continue to wander around its territory
		If  avoiding=Null Then
			abob\wander=1
			abob\Bind=1
			;abob\seek=1
			abob\state=NORMAL
			If abob\speed<1 abob\speed=abob\speed+0.1
			
		End If

		abob\rotation=rotation
		Next
	

End Function

Graphics 800,600,32,2

SetBuffer BackBuffer()
SeedRnd(MilliSecs())

;create one or more bobs
While numbobs=0
	numbobs=Input ("number of bobs?:")
	If numbobs=0 Then Print" 0 is not allowed"
Wend

Local hunter.bob
Local hunted.bob

target=1

While numbobs>1 And target = 1
	target = Rand (numbobs)	
Wend

For f=1 To numbobs
	a.bob=New bob
	a\x=Rand(600)+100
	a\y=Rand(400)+100
	a\heading=Rand (360)
	a\speed=1
	a\wander=1
	If f=1 Then hunter = a
	If f=target Then hunted = a

Next

Bind = 1
wander = 1
avoid = 1
height = 100
width=100
t1=MilliSecs()
t2=MilliSecs()
timer=0

While Not KeyHit(1)
	Cls
	count=0
	Local huntx,hunty
	Local preyx,preyy
	For a.bob =Each bob
		movebob(a)
		
		If wander Then wanderbob(a)
;		If a = hunter Then
;			seekbob(hunter,hunted)
;			huntx=a\x
;			hunty=a\y
;		Else If a= hunted Then
;			preyx=a\x
;			preyy=a\y
;		End If
		If bind Then Bindbob(a,400-width/2,300-height/2,width,height)

		If avoid Then avoidbob(a)
		
		dampbob(a)
		drawbob(a)
	Next
	;Text huntx,hunty,"Hunter"
	;Text preyx,preyy,"Prey"
	drawn=0
	If KeyHit(48) Then Bind=Not Bind
	If KeyHit(17) Then wander=Not wander
	If KeyHit(30) Then avoid=Not avoid
	If KeyHit(203) Then width=width+50
	If KeyHit(205) Then width=width-50
	If KeyHit(200) Then height=height+50
	If KeyHit(208) Then height=height-50
	If height<=0 Then height = 2
	If width<=0 Then width =2
	Text 10,10,"(W)Wandering:"+wander
	Text 10,25,"(B)Territory Binding:"+Bind
	Text 10,40,"(A)Avoidance:"+avoid
	Text 10,55,"(Arrow keys)Change Territory size"
	Text 10,70,"(Esc)Quit"
	t1=t2
	t2=MilliSecs()
	
	timer=timer+(t2-t1)
	If timer>100 FPS#=1000/Float(t2-t1): timer=0
	Text 10,95,FPS
	Flip 0
	
Wend

;~IDEal Editor Parameters:
;~C#Blitz3D