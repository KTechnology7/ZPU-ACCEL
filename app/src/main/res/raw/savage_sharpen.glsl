#version 310 es
layout(local_size_x = 16, local_size_y = 16) in;
layout(binding = 0, rgba8) uniform readonly mediump image2D inputImage;
layout(binding = 1, rgba8) uniform writeonly mediump image2D outputImage;

void main() {
    ivec2 pos = ivec2(gl_GlobalInvocationID.xy);
    vec4 center = imageLoad(inputImage, pos);

    vec4 n = imageLoad(inputImage, pos + ivec2(0, -1));
    vec4 s = imageLoad(inputImage, pos + ivec2(0, 1));
    vec4 e = imageLoad(inputImage, pos + ivec2(1, 0));
    vec4 w = imageLoad(inputImage, pos + ivec2(-1, 0));

    vec4 edge = abs(4.0 * center - n - s - e - w);
    vec4 sharpened = center + 0.3 * edge;

    imageStore(outputImage, pos, clamp(sharpened, 0.0, 1.0));
}
