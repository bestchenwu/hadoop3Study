package hadoopStudy;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FloatSortTest {

    @Test
    public void testFloatSortTest(){
        List<Float> floatList = Arrays.asList(3.2f,2.5f,8.4f,1.2f);
        Optional<Float> max = floatList.stream().max((f1, f2) -> Float.compare(f1, f2));
        System.out.println("max="+max.get());
    }
}
