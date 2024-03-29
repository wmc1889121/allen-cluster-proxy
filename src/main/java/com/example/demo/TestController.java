package com.example.demo;

import com.allen.common.entity.DataVO;
import com.allen.db.proxy.AllenDBProxy;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Api
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private AllenDBProxy allenDBProxy;

    @GetMapping("/{key}")
    public DataVO get(@PathVariable("key") String key) throws Exception {
        return allenDBProxy.get(key);
    }

    @PostMapping
    public Object set(@RequestBody DataVO operate) throws Exception {
        return allenDBProxy.set(operate.getKey(), operate.getVal());
    }

    @GetMapping
    public Object queryPerformance() throws Exception {
        FileReader fr = null;
        BufferedReader br = null;
        List<String> src = new ArrayList<>();
        fr = new FileReader("D:/allen_db/test/test_data_40");
        br = new BufferedReader(fr);
        String s;
        while ((s = br.readLine()) != null) {
            String[] split = s.split(":");
            src.add(split[0]);
        }
        Collections.shuffle(src);
        Map<String, Object> res = parallelRead(src);
        br.close();
        fr.close();
        return res;
    }

    private Map<String, Object> parallelRead(List<String> src) throws Exception {
        Map<String, Object> res = new HashMap<>();
        int tsize = 15;
        CountDownLatch latch = new CountDownLatch(tsize);
        ExecutorService service = Executors.newFixedThreadPool(tsize);
        long begin = System.currentTimeMillis();
        System.out.println("query开始：" + begin);
        service.submit(() -> run(res, latch, 0, 25000, src));
        service.submit(() -> run(res, latch, 25000, 50000, src));
        service.submit(() -> run(res, latch, 50000, 75000, src));
        service.submit(() -> run(res, latch, 75000, 100000, src));
        service.submit(() -> run(res, latch, 100000, 125000, src));
        service.submit(() -> run(res, latch, 125000, 150000, src));
        service.submit(() -> run(res, latch, 150000, 175000, src));
        service.submit(() -> run(res, latch, 175000, 200000, src));
        service.submit(() -> run(res, latch, 200000, 225000, src));
        service.submit(() -> run(res, latch, 225000, 250000, src));
        service.submit(() -> run(res, latch, 250000, 275000, src));
        service.submit(() -> run(res, latch, 275000, 300000, src));
        service.submit(() -> run(res, latch, 300000, 325000, src));
        service.submit(() -> run(res, latch, 325000, 350000, src));
        service.submit(() -> run(res, latch, 350000, 370000, src));

//        service.submit(() -> run(res, latch, 0, 100000, src));
//        service.submit(() -> run(res, latch, 100000, 200000, src));
//        service.submit(() -> run(res, latch, 200000, 300000, src));
//        service.submit(() -> run(res, latch, 300000, 370000, src));
        latch.await();
//        run(res, null, 0, src.size(), src);
        res.put("耗时：", System.currentTimeMillis() - begin);
        return res;
    }

    private void run(Map<String, Object> res, CountDownLatch latch, int begin, int end, List<String> src) {
        try {
            for (int i = begin; i < end; i++) {
                String key = src.get(i);
                try {
                    DataVO vo = allenDBProxy.get(key);
//                    if (vo.getVal() == null) {
//                        res.put(key, "数据丢失");
//                    }
                } catch (Exception e) {
                    res.put(key, e.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (latch != null) latch.countDown();
        }
    }

    public static void main(String[] args) {
        File file = new File("D://tt");
        file.mkdir();
        String[] list = file.list((a, name) -> name.startsWith("data"));
        System.out.println(list);
    }
}
