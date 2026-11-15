# Dhaaga (Android)

Do phone, ek room code. Ek taraf se jo sandesh bheja jaata hai, woh doosre phone ki notification aur home screen widget par aata hai.

## Kaise kaam karta hai

- Room code ek lamba random code hai (10 akshar). Dono phone mein wahi code daalte hain.
- Sandesh free public server `ntfy.sh` ke through jaate hain. Room code hi us room ka naam hai.
- App ek chhoti foreground service chalati hai jo room se connected rehti hai, isliye phone ke upar ek halki "Dhaaga chalu hai" notification dikhti hai. Android ke liye yeh zaroori hai.
- Widget aakhri aaya hua sandesh dikhata hai. Widget par dabane se app khulta hai.

## Chalane ke liye

1. Android Studio (Koala ya naya) mein `dhaaga-android` folder kholiye. Pehli baar Gradle sync mein thoda time lagega.
2. Phone par USB debugging chalu karke Run dabaiye, ya Build > Build APK se APK banakar dono phone mein install kijiye.
3. Pehle phone par naam likhiye aur "Naya room banao" dabaiye. "Code partner ko bhejo" se code bhej dijiye.
4. Doosre phone par naam likhiye, code daaliye aur "Room mein judo" dabaiye.
5. Notification ki ijazat dijiye. Home screen par khaali jagah dabake rakhiye, Widgets mein Dhaaga chuniye.

## Dhyan rakhne ki baatein

- Kuch phones (Xiaomi, Oppo, Vivo, Samsung) background apps band kar dete hain. Settings mein Dhaaga ke liye Battery ko "Unrestricted" kar dijiye, warna sandesh der se aayenge.
- Room code ko raaz rakhiye. Jis ke paas code hai woh us room ke sandesh padh aur bhej sakta hai. Sandesh ntfy.sh ke server se guzarte hain aur wahan kuch ghante cache hote hain.
- Apna server chahiye to `Net.kt` mein `SERVER` badal dijiye. ntfy ko khud host kar sakte hain.
- Yeh code mere paas compile ya phone par test nahi hua hai. Agar build mein koi error aaye to error ka text bhej dijiye, main theek kar dunga.
