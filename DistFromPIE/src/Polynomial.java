import java.util.Arrays;

public class Polynomial {

    int minDegree;
    int maxDegree;
    int[] coefficients;
    int symPoint;
    boolean symType;

    public Polynomial(int smallestBase, int largestBase, int mult, int maxDistanceLeft){
        int size = (largestBase - smallestBase) * mult + 1;
        if (size % 2 == 0){
            symType = false;
            symPoint = size / 2;
        } else {
            symType = true;
            symPoint = size / 2 + 1;
        }
        minDegree = smallestBase * mult;
        symPoint += minDegree - 1;
        maxDegree = Math.min(largestBase * mult, maxDistanceLeft);
        if (symPoint > maxDegree){
            symPoint = maxDegree;
        }
        if (minDegree < maxDegree) {
            coefficients = new int[maxDegree - minDegree + 1];
        } else {
            coefficients = new int[1];
        }
    }

    public Polynomial(ThreadVariables t, int max, int multFactor) {
        this(t,max,multFactor,1);
    }

    public Polynomial(int[] b, int m, int mult){
        this(b,m,mult,1);
    }

    public Polynomial(int[] b, int max, int multFactor, int val){
        int maxAcceptableBase = max / multFactor;
        int currentIndex = b.length - 1;
        while (b[currentIndex] > maxAcceptableBase){
            currentIndex--;//and this would just be moving through the array from right to left
            if (currentIndex < 0){
                coefficients = new int[0];
                return;
            }
        }
        minDegree = b[0]*multFactor;
        maxDegree = b[currentIndex] * multFactor;
        if (minDegree > maxDegree){
            minDegree = maxDegree;
        }
        coefficients = new int[maxDegree - minDegree + 1]; //in C memory would be preallocated for MAX so it wouldn't matter so much - could just go increasing and break
        for (int i = 0; i <= currentIndex; i++){
            coefficients[b[i]*multFactor - minDegree] = val;
        }
    }

    public Polynomial(ThreadVariables t, int max, int multFactor, int val){
        int maxAcceptableBase = max / multFactor;
        int currentBase = t.decreasing[t.size]; //will just be last number if t is an array
        while (currentBase > maxAcceptableBase){
            currentBase = t.decreasing[currentBase]; //and this would just be moving through the array from right to left
        }
        minDegree = t.increasing[0]*multFactor;
        maxDegree = currentBase * multFactor;
        if (minDegree > maxDegree){
            minDegree = maxDegree;
        }
        coefficients = new int[maxDegree - minDegree + 1]; //in C memory would be preallocated for MAX so it wouldn't matter so much - could just go increasing and break
        while (currentBase != 0){
            int deg = currentBase*multFactor;
            coefficients[deg - minDegree] = val;
            currentBase = t.decreasing[currentBase];
        }
    }

    public Polynomial(int min, int max){
        this(min, max, new int[max - min + 1]);
    }

    public Polynomial(int min, int max, int[] numbers){
        minDegree = min;
        maxDegree = max;
        coefficients = numbers;
    }

    public Polynomial(int[] numbers){
        for (int i = 0; i < numbers.length; i++){
            if (numbers[i] != 0){
                minDegree = i;
                break;
            }
        }
        for (int i = numbers.length - 1; i >= 0; i--){
            if (numbers[i] != 0){
                maxDegree = i;
                break;
            }
        }
        coefficients = new int[maxDegree - minDegree + 1];
        for (int i = minDegree; i <= maxDegree; i++){
            coefficients[i - minDegree] = numbers[i];
        }
    }

    static Polynomial scale(Polynomial org, int scale){
        for (int i = 0; i <= org.maxDegree - org.minDegree; i++){
            org.coefficients[i] *= scale;
        }
        return org;
    }

    static Polynomial multiply(Polynomial a, Polynomial b, int maxDist){
        //to be implement with fmpz_poly_mullow
        int newMin = a.minDegree + b.minDegree;
        int newMax = a.maxDegree + b.maxDegree;
        int[] newCoeffs;
        if (newMax <= maxDist){
            //_fmpz_poly_mul
            newCoeffs = new int[newMax - newMin + 1];
            classicMultiply(newCoeffs, a.coefficients, b.coefficients);
        } else if (newMin <= maxDist){
            newMax = maxDist;
            //_fmpz_poly_mullow
            newCoeffs = new int[maxDist - newMin + 1];
            classicMultiplyLow(newCoeffs, a.coefficients, b.coefficients, maxDist);
        } else {
            newCoeffs = new int[0];
        }
        return new Polynomial(newMin, newMax, newCoeffs);
    }

    long[] productWith(long[] a, int maxDist){
        long[] product = new long[maxDist + 1];
        for (int i = minDegree; i <= maxDegree; i++){
            int c = coefficients[i - minDegree];
            if (c != 0){
                for (int j = 0; j <= maxDist - i; j++){
                    product[i + j] += a[j] * c;
                }
            }
        }
        return product;
    }

    int[] productWith(int[] a, int maxDist){
        int[] product = new int[maxDist + 1];
        for (int i = minDegree; i <= maxDegree; i++){
            int c = coefficients[i - minDegree];
            if (c != 0){
                for (int j = 0; j <= maxDist - i; j++){
                    product[i + j] += a[j] * c;
                }
            }
        }
        return product;
    }

    long[] trivialProductWith(int mult, int maxDist){
        long[] product = new long[maxDist + 1];
        for (int i = minDegree; i <= Math.min(maxDegree, maxDist); i++){
            product[i] = coefficients[i - minDegree];
        }
        for (int i = mult; i <= maxDist; i++){
            product[i] += product[i - mult];
        }
        return product;
    }

    static void classicMultiply(int[] dest, int[] a, int[] b){
        //n^2 algorithm but will use faster things in C
        for (int i = 0; i < a.length; i++){
            if (a[i] != 0){
                for (int j = 0; j < b.length; j++){
                    dest[i+j] += a[i] * b[j];
                }
            }
        }
    }

    static void classicMultiplyLow(int[] dest, int[] a, int[] b, int len){
        //n^2 algorithm but will use faster things in C
        for (int i = 0; i < Math.min(a.length, len+1); i++){
            if (a[i] != 0){
                for (int j = 0; j < Math.min(b.length, len + 1 - i); j++){
                    dest[i+j] += a[i] * b[j];
                }
            }
        }
    }

    //can just use virtual pointers or something
    //actually allocate for full array and just pass parts of it to poly mul and add functions and make sure to zero extras
    long[] getFullArray(int size){
        long[] coeffs = new long[size + 1];
        for (int i = minDegree; i <= Math.min(size, maxDegree); i++){
            coeffs[i] = coefficients[i - minDegree];
        }
        return coeffs;
    }

    @Override
    public String toString() {
        return "Polynomial{" +
                "minDegree=" + minDegree +
                ", maxDegree=" + maxDegree +
                ", coefficients=" + Arrays.toString(coefficients) +
                ", symPoint=" + symPoint +
                ", symType=" + symType +
                '}';
    }

    void symPolyMultAddShifted(Polynomial[] polynomials, int[] indexes, int[] missingMults, int num){
        for (int i = 0; i < num; i++){
            Polynomial b = polynomials[indexes[i]];
            Polynomial a = polynomials[missingMults[i]]; //missing mults should always be one of the original polynomials that has lots of zeroes so as a it can be taken advantage of
            classicMultiplyLow(coefficients, a.coefficients, b.coefficients, symPoint);
        }
        finalizeSymmetry(symPoint, symType);
    }

    void symAddShifted(Polynomial a, int shift, int maxDist){
        int s = Math.min(symPoint - shift, maxDist - shift);
        s = Math.min(s, a.maxDegree);
        for (int i = a.minDegree; i <= s; i++){
            coefficients[i + shift - minDegree] += a.coefficients[i - a.minDegree];
        }
    }

    public static Polynomial add(Polynomial a, Polynomial b){
        int newMin = Math.min(a.minDegree, b.minDegree);
        int newMax = Math.max(a.maxDegree, b.maxDegree);
        int[] coeffs = new int[newMax - newMin + 1];
        int i = a.minDegree;
        int j = b.minDegree;
        while (i < j){
            coeffs[i++ - newMin] = a.coefficients[i - a.minDegree];
        }
        while (j < i){
            coeffs[j++ - newMin] = b.coefficients[j - b.minDegree];
        }
        int sharedMax = Math.min(a.maxDegree, b.maxDegree);
        while (i <= sharedMax){
            coeffs[i - newMin] = a.coefficients[i - a.minDegree] + b.coefficients[i - b.minDegree];
            i++;
        }
        j = i;
        while (i < a.maxDegree){
            coeffs[i++ - newMin] = a.coefficients[i - a.maxDegree];
        }
        while (j < b.maxDegree){
            coeffs[j++ - newMin] = b.coefficients[j - b.maxDegree];
        }
        return new Polynomial(newMin, newMax, coeffs);
    }

    void addShifted(Polynomial a, int shift, int stop){
        int s = Math.min(stop - shift, a.maxDegree);
        s = Math.min(s, maxDegree - shift);
        /*if (s == stop - shift){
            System.out.println("Hit max dist");
        } else if (s == a.maxDegree){
            System.out.println("Hit max from value read");
            if (maxDegree - shift == s){
                System.out.println("Also max - shift");
            }
        } else {
            System.out.println("Hit max from target degree");
        }*/
        for (int i = a.minDegree; i <= s; i++){
            coefficients[i + shift - minDegree] += a.coefficients[i - a.minDegree];
        }
    }

    void finalizeSymmetry(){
        finalizeSymmetry(symPoint, symType);
    }

    void finalizeSymmetry(int symmetricPoint, boolean odd){
        if(odd) {
            int s = symmetricPoint - minDegree;
            int l = s;
            if (l > maxDegree - symmetricPoint){
                l = maxDegree - symmetricPoint;
            }
            for (int i = 0; i <= l; i++) {
                coefficients[s + i] = coefficients[s - i];
            }
        } else {
            int s = symmetricPoint - minDegree;
            int l = s;
            if (l > maxDegree - symmetricPoint){
                l = maxDegree - symmetricPoint;
            }
            for (int i = 0; i <= l; i++){
                coefficients[s + i + 1] = coefficients[s - i];
            }
        }
    }

    void setCoeff(int c, int val){
        if (c <= maxDegree) {
            coefficients[c - minDegree] = val;
        }
    }
}
