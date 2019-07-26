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
        List<String> src = new LinkedList<>();
        fr = new FileReader("D:/allen_db/test_data_10");
        br = new BufferedReader(fr);
        String s;
        while ((s = br.readLine()) != null) {
            String[] split = s.split(":");
            src.add(split[0]);
        }
        Collections.shuffle(src);
        Map<String, Object> res = new HashMap<>();
        long begin = System.currentTimeMillis();
        for (String key : src) {
            try {
                DataVO vo = allenDBProxy.get(key);
                if (vo.getVal() == null) {
                    res.put(key, "数据丢失");
                }
            } catch (Exception e) {
                res.put(key, e);
            }
        }
        res.put("耗时：", System.currentTimeMillis() - begin);
        br.close();
        fr.close();
        return res;
    }

    public static void main(String[] args) {
        File file = new File("D://tt");
        file.mkdir();
        String[] list = file.list((a, name) -> name.startsWith("data"));
        System.out.println(list);
    }
}
