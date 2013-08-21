
float W (float4 r, float h) {

	float x = length(r) / h;
	float k = 2.546479089 / pown(h,3);
	
	if (x > 1) return 0;
	if (x < 0.5) return k * (6 * (pown(x,3) - pown(x,2)) + 1);
	return k * 2 * pown(1-x,3);
}

float4 gradW (float4 r, float h) {

	float x = length(r) / h;
	float k = 6 * 2.546479089 / pown(h,4);
	float4 r_norm = r / length(r);
	
	if (x > 1) return (float4)0;
	if (x < 0.5) return k * (3 * pown(x,2) - 2 * x) * r_norm;
	return -k * pown(1-x,2) * r_norm;
}


kernel void sph_CalcNewRho(
global float4* body_Pos,
global float* body_rho,
global float4* body_V,
const float DELTA_T,
const float m
)
{
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	float h = 0.2;
	float delta_rho = 0;
	
	
	float rho = 0;
	
	for (int i = 0; i < N; i++) {
		
		rho += m * W(body_Pos[id]-body_Pos[i], h);
	}
	
	body_rho[id] = rho;
	
	
	/*
	for (int i = 0; i < N; i++) {
		
		if (id != i)
		delta_rho += dot(body_V[id]-body_V[i], gradW(body_Pos[id]-body_Pos[i], h));
	}
	
	delta_rho *= m;
	delta_rho *= DELTA_T;
	body_rho[id] += delta_rho;
	*/
}


kernel void sph_CalcNewP(
global float* body_P,
global float* body_rho,
const float rho,
const float m,
const float c,
const float gamma
)
{
	uint id = get_global_id(0);
	body_P[id] = (pown(body_rho[id]/rho, 7)-1);
	//body_P[id] = (body_rho[id] - rho) * 1000000;
}

kernel void sph_CalcNewV(
global float4* body_Pos,
global float4* body_V,
const float DELTA_T,
const float EPSILON_SQUARED,
global float* body_P,
global float* body_rho,
const float m
)
{
	
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	
	float h = 0.2;
	float alpha = 1;
	float eta = 0.001;
	float c = 10;
	float4 g = (float4)(0,-1000,0,0);
	float4 accel = (float4)0;
	
	
	for (int i = 0; i < N; i++) {

		float visc = 0;
		
		
		float tmp = dot(body_V[id]-body_V[i], body_Pos[id]-body_Pos[i]);
		if (tmp > 0) {
			visc = - 2 * alpha * h * c * tmp / 
					((body_rho[id] + body_rho[i]) * (eta * eta + dot(body_Pos[id]-body_Pos[i], body_Pos[id]-body_Pos[i])));
		}
		
		float4 r = body_Pos[id]-body_Pos[i];
		float4 grad = gradW(r, h);
		float C = (body_P[i]/(body_rho[i] * body_rho[i]) + body_P[id]/(body_rho[id] * body_rho[id]) + visc);
		if (length(C * grad) < 1000000000) {
			accel += C * grad;
		}
	}

	accel *= m;
	accel += g;

	//-------------------------------
	//		boundery forces
	//-------------------------------
	
	float4 pos = body_Pos[id];
	float r[5];
	float4 b[5];
	b[0] = (float4)(pos.x, -1, pos.z, 0);
	b[1] = (float4)(pos.x, pos.y, -1, 0);
	b[2] = (float4)(pos.x, pos.y, 1, 0);
	b[3] = (float4)(-1, pos.y, pos.z, 0);
	b[4] = (float4)(1, pos.y, pos.z, 0);
	
	for (int i = 0; i < 5; i++) {
		r[i] = distance(pos, b[i]);

		if (r[i] < 0.2) {
			accel += (10 * (pown(0.2/r[i], 4) - pown(0.2/r[i], 2)) / (r[i] * r[i])) *  (pos - b[i]);
		}
	
	}
	
	//-------------------------------
	
	body_V[id] += accel * DELTA_T;
}


kernel void sph_CalcNewPos(
global float4* body_Pos, 
global float4* body_V,
const float DELTA_T,
global float* body_rho,
const float m
)
{
    uint id = get_global_id(0);
    body_Pos[id] += body_V[id] * DELTA_T;
}