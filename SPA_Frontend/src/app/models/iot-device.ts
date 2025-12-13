export interface IoT {
  _id: string, // Mongodb Object ID
  name: string,
  setTemp: number,
  currentTemp: number,
  lastChecked: Date,
  state: string
}
