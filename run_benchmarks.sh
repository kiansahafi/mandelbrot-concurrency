PROJECT_DIR="/Users/kiansahafi/Desktop/Classes/Concurrency Multi-core Programming and Data Processing/Final_Project/kian_src"
RESULTS_FILE="mandelbrot_benchmark_results.csv"
LOG_FILE="mandelbrot_benchmark.log"

cd "$PROJECT_DIR" || {
    echo "Error: Cannot access project directory"
    exit 1
}

echo "Compiling Java files..."
javac *.java || {
    echo "Error: Compilation failed"
    exit 1
}

echo "Iterations,SchedulingPolicy,Threads,ChunkSize,ChunkMethod,TimeTaken" > "$RESULTS_FILE"
echo "Starting benchmark at $(date)" > "$LOG_FILE"

# parameters

ITERATIONS=(1000 10000)
SCHEDULING_POLICIES=("Static-block" "Static-cyclic" "Dynamic" "Guided")
THREADS=(1 2 4 8)
CHUNK_SIZES=(1 50 100 720)
CHUNK_METHODS=("by Row" "by Column")

total_tests=$((${#ITERATIONS[@]} * ${#SCHEDULING_POLICIES[@]} * ${#THREADS[@]} * 
             ${#CHUNK_SIZES[@]} * ${#CHUNK_METHODS[@]}))
current_test=0

echo "Running $total_tests tests..."

# Run benchmark
for iteration in "${ITERATIONS[@]}"; do
    for policy in "${SCHEDULING_POLICIES[@]}"; do
        for thread in "${THREADS[@]}"; do
            for chunk_size in "${CHUNK_SIZES[@]}"; do
                for method in "${CHUNK_METHODS[@]}"; do
                    current_test=$((current_test + 1))
                    progress=$((current_test * 100 / total_tests))
                    
                    echo "[$progress%] Test $current_test/$total_tests: $iteration iterations, $policy policy, $thread threads, chunk size $chunk_size, $method"
                    
                    # Run Java program
                    output=$(java main "$iteration" "$policy" "$thread" "$chunk_size" "$method" 2>&1)
                    
                    # Extract execution time
                    time_taken=$(echo "$output" | grep "Total time taken:" | awk '{print $4}' | sed 's/s$//')
                    
                    # Log results
                    echo "$iteration,$policy,$thread,$chunk_size,$method,$time_taken" >> "$RESULTS_FILE"
                    echo "Time: $time_taken seconds"
                    
                    # Save output to log
                    echo -e "\n--- Test $current_test/$total_tests ---" >> "$LOG_FILE"
                    echo "$output" >> "$LOG_FILE"
                    
                    # Create a image filename based on configuration
                    method_short=$(echo "$method" | sed 's/ /_/g')
                    new_image_name="iter${iteration}_${policy}_threads${thread}_chunk${chunk_size}_${method_short}.png"
                    
                    if [ -f "mandelbrot.png" ]; then
                        mv mandelbrot.png "images/$new_image_name"
                        echo "Image saved as: $new_image_name"
                    else
                        echo "Warning: mandelbrot.png not found"
                    fi
                    
                    sleep 1
                done
            done
        done
    done
done

echo "Benchmark completed! Results saved to $RESULTS_FILE"

# Generate simple analysis
echo -e "\n--- Performance Summary ---" | tee -a "$LOG_FILE"
echo "Top 5 fastest configurations:" | tee -a "$LOG_FILE"
sort -t, -k7,7n "$RESULTS_FILE" | head -6 | column -t -s, | tee -a "$LOG_FILE"
echo -e "\nTop 5 slowest configurations:" | tee -a "$LOG_FILE"
sort -t, -k7,7nr "$RESULTS_FILE" | head -6 | column -t -s, | tee -a "$LOG_FILE"