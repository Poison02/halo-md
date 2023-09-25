# 手写RPC（3）

🚀首先放上我的GitHub的完整项目地址：[https://github.com/Poison02/rpc-framework](https://github.com/Poison02/rpc-framework)

OK，承接上一篇代理层过后，我们继续向后看，我们客户端经过代理之后应该做什么呢？这时候我们发送的是原数据，也就是可能含有对象的，那我们在网络传输中，直接发送对象肯定是不行的，这时候就轮到我们的序列化层登场了，我们将原数据序列化为字节进行传输明显体积会减小，传输速度肯定也会有所增长的。

# 序列化

这里先列举常见的序列化方式：

- `JDK序列化`
- `JSON序列化`
- `Hessian序列化`
- `Kryo序列化`
- `ProtoStuff序列化`
- ...

接下来我们将这几种方式测试对比一下。

为了更方便，我们定义一个父接口：

```java
public interface SerializeFactory {

    /**
     * 序列化
     *
     * @param t
     * @param <T>
     * @return
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] data, Class<T> clazz);

}
```

接下来我们创建一个对象 ，用这个对象进行序列化测试

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private int age;

    private String address;

    private long bankNo;

    private int sex;

    private int id;

    private String idCardNo;

    private String remark;

    private String username;

}
```



## JDK序列化

关于JDK序列化的测试案例如下：

```java
public class JdkSerializeFactory implements SerializeFactory{

    @Override
    public <T> byte[] serialize(T t) {
        byte[] data = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(os);
            output.writeObject(t);
            output.flush();
            output.close();
            data = os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        try {
            ObjectInputStream input = new ObjectInputStream(is);
            Object result = input.readObject();
            return ((T) result);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
```

接下来我们新建一个测试类对他进行测试

```java
public class Test {

    private static User buildUserDefault() {
        User user = new User();
        user.setAge(11);
        user.setAddress("成都市龙泉驿区");
        user.setBankNo(12897873624813L);
        user.setSex(1);
        user.setId(10001);
        user.setIdCardNo("440308781129381222");
        user.setRemark("备注信息字段");
        user.setUsername("ddd-user-name");
        return user;
    }

    public void jdkSerializeSizeTest() {
        SerializeFactory serializeFactory = new JdkSerializeFactory();
        User user = buildUserDefault();
        byte[] result = serializeFactory.serialize(user);
        System.out.println("jdk's size is "+result.length);
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.jdkSerializeSizeTest();
    }

}
```

观察输出为：

```
jdk's size is 248
```

## JSON序列化

接下来我们测试JSON序列化，这里我是用FastJson进行测试，我们只需要导入相关依赖即可：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.33</version>
</dependency>
```

```java
public class FastJsonSerializeFactory implements SerializeFactory {

    @Override
    public <T> byte[] serialize(T t) {
        String jsonStr = JSON.toJSONString(t);
        return jsonStr.getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return JSON.parseObject(new String(data),clazz);
    }


}
```

在上面的测试程序之上增加方法然后进行测试即可：

```java
public void fastJsonSerializeSizeTest() {
        SerializeFactory serializeFactory = new FastJsonSerializeFactory();
        User user = buildUserDefault();
        byte[] result = serializeFactory.serialize(user);
        User deserializeUser = serializeFactory.deserialize(result, User.class);
        System.out.println("fastJson's size is "+result.length);
}

public static void main(String[] args) {
        Test test = new Test();
        test.fastJsonSerializeSizeTest();
}
```

可以看到输出结果为：

```
fastJson's size is 176
```

明显比JDK序列化生成的字节小很多了。

## Hessian序列化

同样我们导入依赖，然后书写序列化类

```xml
<dependency>
     <groupId>com.caucho</groupId>
     <artifactId>hessian</artifactId>
     <version>4.0.65</version>
</dependency>
```

```java
public class HessianSerializeFactory implements SerializeFactory {

    @Override
    public <T> byte[] serialize(T t) {
        byte[] data = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Hessian2Output output = new Hessian2Output(os);
            output.writeObject(t);
            output.getBytesOutputStream().flush();
            output.completeMessage();
            output.close();
            data = os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null) {
            return null;
        }
        Object result = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            Hessian2Input input = new Hessian2Input(is);
            result = input.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) result;
    }

}
```

同样的，书写测试用例：

```java
public void hessianSerializeSizeTest() {
        SerializeFactory serializeFactory = new HessianSerializeFactory();
        User user = buildUserDefault();
        byte[] result = serializeFactory.serialize(user);
        User deserializeUser = serializeFactory.deserialize(result, User.class);
        System.out.println("Hessian's size is "+result.length);
}

public static void main(String[] args) {
        Test test = new Test();
        test.jdkSerializeSizeTest();
        test.fastJsonSerializeSizeTest();
        test.hessianSerializeSizeTest();
}
```

看到输出结果为：

```
Hessian's size is 169
```

## Kryo序列化

导入依赖并书写序列化类

```xml
<dependency>
    <groupId>com.esotericsoftware</groupId>
    <artifactId>kryo</artifactId>
    <version>4.0.2</version>
</dependency>
```

```java
public class KryoSerializeFactory implements SerializeFactory {

    private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            return kryo;
        }
    };

    @Override
    public <T> byte[] serialize(T t) {
        Output output = null;
        try {
            Kryo kryo = kryos.get();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            output = new Output(byteArrayOutputStream);
            kryo.writeClassAndObject(output, t);
            return output.toBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Input input = null;
        try {
            Kryo kryo = kryos.get();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            input = new Input(byteArrayInputStream);
            return (T) kryo.readClassAndObject(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }
}
```

书写测试用例进行测试：

```java
public void kryoSerializeSizeTest() {
        SerializeFactory serializeFactory = new KryoSerializeFactory();
        User user = buildUserDefault();
        byte[] result = serializeFactory.serialize(user);
        User deserializeUser = serializeFactory.deserialize(result, User.class);
        System.out.println("Kryo's size is "+result.length);
}

public static void main(String[] args) {
        Test test = new Test();
        test.kryoSerializeSizeTest();
}
```

测试结果为：

```
Kryo's size is 113
```

可以看到又减少了不少字节。

## ProtoStuff序列化

导入依赖并书写方法：

```xml
<dependency>
    <groupId>io.protostuff</groupId>
    <artifactId>protostuff-core</artifactId>
    <version>1.7.2</version>
</dependency>
<dependency>
    <groupId>io.protostuff</groupId>
    <artifactId>protostuff-runtime</artifactId>
    <version>1.7.2</version>
</dependency>
```

```java
public class ProtoStuffSerializeFactory implements SerializeFactory{

    /**
     * 避免每次序列化时重新申请缓冲区空间
     */
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    @Override
    public <T> byte[] serialize(T t) {
        Class<?> clazz = t.getClass();
        Schema schema = RuntimeSchema.getSchema(clazz);
        byte[] bytes;
        try {
            bytes = ProtostuffIOUtil.toByteArray(t, schema, BUFFER);
        } finally {
            BUFFER.clear();
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }
}
```

书写测试用例并测试

```java
public void ProtoStuffSerializeSizeTest() {
        SerializeFactory serializeFactory = new ProtoStuffSerializeFactory();
        User user = buildUserDefault();
        byte[] result = serializeFactory.serialize(user);
        User deserializeUser = serializeFactory.deserialize(result, User.class);
        System.out.println("ProtoStuff's size is "+result.length);
}

public static void main(String[] args) {
        Test test = new Test();
        test.ProtoStuffSerializeSizeTest();
}
```

测试结果为：

```
ProtoStuff's size is 93
```

## 总结

可以看到JDK后面几种序列化方式简直相比JDK而言都是很不错的。因此从这个简单的测试我们就能看出除了JDK序列化以外，其他集中序列化方式都是我们值得使用的。但是本项目并没有使用JSON序列化，总结来说就是懒罢了...

# 压缩方式

除了序列化以外，我们还可以对序列化之后的数据进行压缩，使其进一步缩小体积，这里我就只使用一个压缩方法--`GZIP`。

话不多说，直接上测试，这里就不用导入依赖了，我们使用 `java.util.zip` 包下面的类进行操作即可：

```java
public class GZIPCompress {

    private static final int BUFFER_SIZE = 1024 * 4;

    public byte[] compress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip compress error", e);
        }
    }

    public byte[] decompress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPInputStream gunzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = gunzip.read(buffer)) > -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip decompress error", e);
        }
    }

}
```

测试类：

```java
public void GZIPTest() {
        SerializeFactory serializeFactory = new ProtoStuffSerializeFactory();
        User user = buildUserDefault();
        byte[] result = serializeFactory.serialize(user);
        System.out.println("ProtoStuff's size is "+result.length);

        GZIPCompress gzipCompress = new GZIPCompress();
        byte[] compress = gzipCompress.compress(result);
        System.out.println("compress's size is: " + compress.length);
}


public static void main(String[] args) {
        Test test = new Test();
        test.GZIPTest();
}
```

压缩的话，其实要字节比较多的情况下效果才显著，看个人意愿选择是否压缩吧。

# 结尾

OK，今天主要写一下序列化以及压缩这一块，下一章将会写客户端与服务端进行通信。