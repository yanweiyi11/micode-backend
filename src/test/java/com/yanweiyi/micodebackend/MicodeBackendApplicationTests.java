package com.yanweiyi.micodebackend;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
class MicodeBackendApplicationTests {

    @Test
    void testJudge() {
        String sourceCode =
                "import java.util.Scanner;\n" +
                        "public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        int a = scanner.nextInt();\n" +
                        "        int b = scanner.nextInt();\n" +
                        "        int sum = a + b;\n" +
                        "        System.out.println(sum);\n" +
                        "    }\n" +
                        "}";
        String fileName = "Main.java";

        List<String> inputList = Arrays.asList("1 2", "3 4", "-1 1");

        // 步骤 1: 写入程序到文件
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(sourceCode);
        } catch (FileNotFoundException e) {
            log.error("file write failure: {}", e.getMessage());
            return;
        }

        try {
            // 步骤 2: 编译文件
            Process compileProcess = Runtime.getRuntime().exec("javac " + fileName);
            compileProcess.waitFor(); // 等待编译完成

            // 检查编译是否成功
            if (compileProcess.exitValue() == 0) {
                // 步骤 3: 循环执行编译后的文件，并向其提供输入
                for (String input : inputList) {
                    Process runProcess = Runtime.getRuntime().exec("java Main");

                    // 向运行的程序提供输入
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                        String join = StrUtil.replace(input, " ", "\n") + "\n";
                        writer.write(join);
                        writer.flush();
                    }

                    // 读取并输出执行结果
                    BufferedReader in = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                    in.close();
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error("execution error: {}", e.getMessage());
        }
    }

}
